package com.keepr.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepr.data.model.VaultEntry
import com.keepr.data.repository.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailState(
    val entry: VaultEntry? = null,
    val passwordVisible: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val deleted: Boolean = false,
    val copiedField: String? = null
)

class DetailViewModel(
    private val repository: VaultRepository,
    private val entryId: String
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val entry = repository.getEntryById(entryId)
            _state.update { it.copy(entry = entry) }
        }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun toggleFavorite() {
        val entry = _state.value.entry ?: return
        viewModelScope.launch {
            val updated = entry.copy(isFavorite = !entry.isFavorite)
            repository.updateEntry(updated)
            _state.update { it.copy(entry = updated) }
        }
    }

    fun showDeleteConfirm() = _state.update { it.copy(showDeleteConfirm = true) }
    fun hideDeleteConfirm() = _state.update { it.copy(showDeleteConfirm = false) }

    fun deleteEntry() {
        val entry = _state.value.entry ?: return
        viewModelScope.launch {
            repository.deleteEntry(entry)
            _state.update { it.copy(deleted = true) }
        }
    }

    fun onFieldCopied(field: String) {
        _state.update { it.copy(copiedField = field) }
    }

    fun clearCopiedField() {
        _state.update { it.copy(copiedField = null) }
    }
}
