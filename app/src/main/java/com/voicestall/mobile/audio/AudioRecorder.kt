package com.voicestall.mobile.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

sealed class AudioState {
    data object Idle : AudioState()
    data object Recording : AudioState()
    data class Stopped(val filePath: String) : AudioState()
    data class Error(val message: String) : AudioState()
}

class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var recordingStartTime: Long = 0L

    private val _state = MutableStateFlow<AudioState>(AudioState.Idle)
    val state: StateFlow<AudioState> = _state.asStateFlow()

    private val _amplitude = MutableStateFlow(0)
    val amplitude: StateFlow<Int> = _amplitude.asStateFlow()

    fun startRecording() {
        try {
            val file = File(context.cacheDir, "recording_${System.currentTimeMillis()}.m4a")
            outputFile = file

            recorder = createMediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setAudioEncodingBitRate(128000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            recordingStartTime = System.currentTimeMillis()
            _state.value = AudioState.Recording
        } catch (e: Exception) {
            _state.value = AudioState.Error("Failed to start recording: ${e.message}")
            cleanup()
        }
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null

            val file = outputFile
            if (file != null && file.exists() && file.length() > 0) {
                _state.value = AudioState.Stopped(file.absolutePath)
            } else {
                _state.value = AudioState.Error("Recording file is empty or missing")
            }
        } catch (e: Exception) {
            _state.value = AudioState.Error("Failed to stop recording: ${e.message}")
            cleanup()
        }
    }

    fun getRecordingDurationMs(): Long {
        return if (recordingStartTime > 0) {
            System.currentTimeMillis() - recordingStartTime
        } else 0L
    }

    fun pollAmplitude(): Int {
        return try {
            val amp = recorder?.maxAmplitude ?: 0
            _amplitude.value = amp
            amp
        } catch (e: Exception) {
            0
        }
    }

    fun reset() {
        cleanup()
        _state.value = AudioState.Idle
        _amplitude.value = 0
    }

    fun deleteRecordingFile() {
        outputFile?.delete()
        outputFile = null
    }

    private fun cleanup() {
        try {
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null
    }

    @Suppress("DEPRECATION")
    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }
}
