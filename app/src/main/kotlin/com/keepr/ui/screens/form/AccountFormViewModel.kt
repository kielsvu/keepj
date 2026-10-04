package com.keepr.ui.screens.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepr.data.model.VaultCategory
import com.keepr.data.model.VaultEntry
import com.keepr.data.repository.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormState(
    val serviceName: String = "",
    val accountLabel: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val website: String = "",
    val category: String = VaultCategory.OTHER.displayName,
    val notes: String = "",
    val isFavorite: Boolean = false,
    val isEditing: Boolean = false,
    val saved: Boolean = false
)

class AccountFormViewModel(
    private val repository: VaultRepository,
    private val editEntryId: String? = null
) : ViewModel() {

    private val _state = MutableStateFlow(FormState())
    val state: StateFlow<FormState> = _state.asStateFlow()

    private var originalEntry: VaultEntry? = null

    init {
        if (editEntryId != null) {
            viewModelScope.launch {
                val entry = repository.getEntryById(editEntryId)
                if (entry != null) {
                    originalEntry = entry
                    _state.update {
                        it.copy(
                            serviceName = entry.serviceName,
                            accountLabel = entry.accountLabel,
                            username = entry.username,
                            email = entry.email,
                            password = entry.passwordEncrypted,
                            website = entry.website,
                            category = entry.category,
                            notes = entry.notes,
                            isFavorite = entry.isFavorite,
                            isEditing = true
                        )
                    }
                }
            }
        }
    }

    fun onServiceName(v: String) = _state.update { it.copy(serviceName = v) }
    fun onAccountLabel(v: String) = _state.update { it.copy(accountLabel = v) }
    fun onUsername(v: String) = _state.update { it.copy(username = v) }
    fun onEmail(v: String) = _state.update { it.copy(email = v) }
    fun onPassword(v: String) = _state.update { it.copy(password = v) }
    fun onWebsite(v: String) = _state.update { it.copy(website = v) }
    fun onCategory(v: String) = _state.update { it.copy(category = v) }
    fun onNotes(v: String) = _state.update { it.copy(notes = v) }
    fun onFavorite(v: Boolean) = _state.update { it.copy(isFavorite = v) }

    fun applyGeneratedPassword(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun save() {
        val s = _state.value
        if (s.serviceName.isBlank()) return

        viewModelScope.launch {
            if (s.isEditing && originalEntry != null) {
                val updated = originalEntry!!.copy(
                    serviceName = s.serviceName.trim(),
                    accountLabel = s.accountLabel.trim(),
                    username = s.username.trim(),
                    email = s.email.trim(),
                    passwordEncrypted = s.password,
                    website = s.website.trim(),
                    category = s.category,
                    notes = s.notes.trim(),
                    isFavorite = s.isFavorite,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateEntry(updated)
            } else {
                val entry = VaultEntry(
                    serviceName = s.serviceName.trim(),
                    accountLabel = s.accountLabel.trim(),
                    username = s.username.trim(),
                    email = s.email.trim(),
                    passwordEncrypted = s.password,
                    website = s.website.trim(),
                    category = s.category,
                    notes = s.notes.trim(),
                    isFavorite = s.isFavorite
                )
                repository.insertEntry(entry)
            }
            _state.update { it.copy(saved = true) }
        }
    }
}
