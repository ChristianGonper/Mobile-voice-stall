package com.voicestall.mobile.ui.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicestall.mobile.data.local.entity.DictionaryEntryEntity
import com.voicestall.mobile.data.repository.DictionaryRepository
import com.voicestall.mobile.util.DictionaryProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DictionaryDialogState(
    val isVisible: Boolean = false,
    val editingEntry: DictionaryEntryEntity? = null,
    val pattern: String = "",
    val replacement: String = "",
    val patternError: String? = null
)

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) : ViewModel() {

    val entries: StateFlow<List<DictionaryEntryEntity>> = dictionaryRepository.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dialogState = MutableStateFlow(DictionaryDialogState())
    val dialogState: StateFlow<DictionaryDialogState> = _dialogState.asStateFlow()

    fun showAddDialog() {
        _dialogState.value = DictionaryDialogState(isVisible = true)
    }

    fun showEditDialog(entry: DictionaryEntryEntity) {
        _dialogState.value = DictionaryDialogState(
            isVisible = true,
            editingEntry = entry,
            pattern = entry.pattern,
            replacement = entry.replacement
        )
    }

    fun dismissDialog() {
        _dialogState.value = DictionaryDialogState()
    }

    fun updatePattern(pattern: String) {
        val error = if (pattern.isNotBlank() && !DictionaryProcessor.isValidRegex(pattern)) {
            "Invalid regex pattern"
        } else null
        _dialogState.value = _dialogState.value.copy(pattern = pattern, patternError = error)
    }

    fun updateReplacement(replacement: String) {
        _dialogState.value = _dialogState.value.copy(replacement = replacement)
    }

    fun saveEntry() {
        val state = _dialogState.value
        if (state.pattern.isBlank() || state.patternError != null) return

        viewModelScope.launch {
            val existing = state.editingEntry
            if (existing != null) {
                dictionaryRepository.updateEntry(
                    existing.copy(pattern = state.pattern, replacement = state.replacement)
                )
            } else {
                dictionaryRepository.addEntry(state.pattern, state.replacement)
            }
            dismissDialog()
        }
    }

    fun deleteEntry(entry: DictionaryEntryEntity) {
        viewModelScope.launch {
            dictionaryRepository.deleteEntry(entry)
        }
    }

    fun toggleEntry(entry: DictionaryEntryEntity) {
        viewModelScope.launch {
            dictionaryRepository.updateEntry(entry.copy(enabled = !entry.enabled))
        }
    }
}
