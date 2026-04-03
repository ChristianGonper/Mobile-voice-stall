package com.voicestall.mobile.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicestall.mobile.audio.AudioRecorder
import com.voicestall.mobile.audio.AudioState
import com.voicestall.mobile.data.local.entity.TranscriptionEntity
import com.voicestall.mobile.data.repository.TranscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isRecording: Boolean = false,
    val isProcessing: Boolean = false,
    val recordingDurationSec: Int = 0,
    val lastTranscription: TranscriptionEntity? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val transcriptionRepository: TranscriptionRepository
) : AndroidViewModel(application) {

    val audioRecorder = AudioRecorder(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            audioRecorder.state.collect { audioState ->
                when (audioState) {
                    is AudioState.Idle -> {
                        _uiState.value = _uiState.value.copy(
                            isRecording = false,
                            isProcessing = false,
                            recordingDurationSec = 0
                        )
                    }
                    is AudioState.Recording -> {
                        _uiState.value = _uiState.value.copy(
                            isRecording = true,
                            isProcessing = false,
                            error = null
                        )
                        startTimer()
                    }
                    is AudioState.Stopped -> {
                        stopTimer()
                        _uiState.value = _uiState.value.copy(
                            isRecording = false,
                            isProcessing = true
                        )
                        processRecording(audioState.filePath)
                    }
                    is AudioState.Error -> {
                        stopTimer()
                        _uiState.value = _uiState.value.copy(
                            isRecording = false,
                            isProcessing = false,
                            error = audioState.message
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            val latest = transcriptionRepository.getLatest()
            _uiState.value = _uiState.value.copy(lastTranscription = latest)
        }
    }

    fun toggleRecording() {
        when (audioRecorder.state.value) {
            is AudioState.Recording -> audioRecorder.stopRecording()
            is AudioState.Idle, is AudioState.Error -> {
                audioRecorder.reset()
                audioRecorder.startRecording()
            }
            is AudioState.Stopped -> {
                audioRecorder.reset()
                audioRecorder.startRecording()
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun processRecording(filePath: String) {
        viewModelScope.launch {
            val durationMs = audioRecorder.getRecordingDurationMs()
            val result = transcriptionRepository.transcribe(filePath, durationMs)

            result.fold(
                onSuccess = { entity ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        lastTranscription = entity,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = error.message ?: "Transcription failed"
                    )
                }
            )

            audioRecorder.deleteRecordingFile()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var seconds = 0
            while (isActive) {
                _uiState.value = _uiState.value.copy(recordingDurationSec = seconds)
                delay(1000)
                seconds++
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}
