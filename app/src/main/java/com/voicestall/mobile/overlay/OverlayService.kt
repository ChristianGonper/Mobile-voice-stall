package com.voicestall.mobile.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.voicestall.mobile.MainActivity
import com.voicestall.mobile.R
import com.voicestall.mobile.audio.AudioRecorder
import com.voicestall.mobile.audio.AudioState
import com.voicestall.mobile.data.repository.TranscriptionRepository
import com.voicestall.mobile.util.ClipboardHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OverlayService : Service() {

    companion object {
        const val CHANNEL_ID = "voice_stall_overlay"
        const val NOTIFICATION_ID = 1
        var isRunning = false
            private set
    }

    @Inject
    lateinit var transcriptionRepository: TranscriptionRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var windowManager: WindowManager
    private lateinit var audioRecorder: AudioRecorder
    private var overlayView: View? = null
    private var lastTranscription: String? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        audioRecorder = AudioRecorder(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        showOverlay()
        observeAudioState()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        removeOverlay()
        audioRecorder.reset()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_mic)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun showOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.overlay_bubble, null)

        val micButton = overlayView!!.findViewById<ImageButton>(R.id.btn_mic)
        val pasteButton = overlayView!!.findViewById<ImageButton>(R.id.btn_paste)

        micButton.setOnClickListener { onMicClicked(micButton) }
        pasteButton.setOnClickListener { onPasteClicked() }

        // Make the bubble draggable
        overlayView!!.setOnTouchListener(object : View.OnTouchListener {
            private var isDragging = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isDragging = false
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY
                        if (dx * dx + dy * dy > 25) {
                            isDragging = true
                        }
                        if (isDragging) {
                            params.x = initialX + dx.toInt()
                            params.y = initialY + dy.toInt()
                            windowManager.updateViewLayout(overlayView, params)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            v.performClick()
                        }
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(overlayView, params)
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        overlayView = null
    }

    private fun onMicClicked(micButton: ImageButton) {
        when (audioRecorder.state.value) {
            is AudioState.Recording -> {
                audioRecorder.stopRecording()
            }
            is AudioState.Idle, is AudioState.Error, is AudioState.Stopped -> {
                audioRecorder.reset()
                audioRecorder.startRecording()
                micButton.setImageResource(R.drawable.ic_mic_recording)
            }
        }
    }

    private fun onPasteClicked() {
        val text = lastTranscription
        if (text != null) {
            ClipboardHelper.copyToClipboard(this, text)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No transcription available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeAudioState() {
        serviceScope.launch {
            audioRecorder.state.collect { state ->
                when (state) {
                    is AudioState.Stopped -> {
                        updateMicIcon(processing = true)
                        val durationMs = audioRecorder.getRecordingDurationMs()
                        val result = transcriptionRepository.transcribe(state.filePath, durationMs)
                        result.fold(
                            onSuccess = { entity ->
                                lastTranscription = entity.text
                                ClipboardHelper.copyToClipboard(this@OverlayService, entity.text)
                                Toast.makeText(
                                    this@OverlayService,
                                    "Transcribed: ${entity.text.take(50)}...",
                                    Toast.LENGTH_LONG
                                ).show()
                                updateMicIcon(processing = false)
                            },
                            onFailure = { error ->
                                Toast.makeText(
                                    this@OverlayService,
                                    "Error: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                updateMicIcon(processing = false)
                            }
                        )
                        audioRecorder.deleteRecordingFile()
                    }
                    is AudioState.Error -> {
                        Toast.makeText(this@OverlayService, state.message, Toast.LENGTH_SHORT).show()
                        updateMicIcon(processing = false)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun updateMicIcon(processing: Boolean) {
        overlayView?.findViewById<ImageButton>(R.id.btn_mic)?.apply {
            setImageResource(if (processing) R.drawable.ic_mic_recording else R.drawable.ic_mic)
            alpha = if (processing) 0.5f else 1.0f
        }
    }
}
