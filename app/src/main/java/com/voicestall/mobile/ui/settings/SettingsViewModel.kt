package com.voicestall.mobile.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicestall.mobile.data.preferences.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val apiKey: String = "",
    val selectedModel: String = SettingsDataStore.DEFAULT_MODEL,
    val language: String = "",
    val availableModels: List<String> = SettingsDataStore.AVAILABLE_MODELS,
    val saved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.apiKey.collect { key ->
                _uiState.value = _uiState.value.copy(apiKey = key)
            }
        }
        viewModelScope.launch {
            settingsDataStore.model.collect { model ->
                _uiState.value = _uiState.value.copy(selectedModel = model)
            }
        }
        viewModelScope.launch {
            settingsDataStore.language.collect { lang ->
                _uiState.value = _uiState.value.copy(language = lang)
            }
        }
    }

    fun updateApiKey(key: String) {
        _uiState.value = _uiState.value.copy(apiKey = key, saved = false)
    }

    fun updateModel(model: String) {
        _uiState.value = _uiState.value.copy(selectedModel = model, saved = false)
    }

    fun updateLanguage(language: String) {
        _uiState.value = _uiState.value.copy(language = language, saved = false)
    }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            settingsDataStore.setApiKey(state.apiKey)
            settingsDataStore.setModel(state.selectedModel)
            settingsDataStore.setLanguage(state.language)
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }
}
