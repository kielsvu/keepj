package com.keepr.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepr.data.model.VaultEntry
import com.keepr.data.repository.SortOrder
import com.keepr.data.repository.VaultRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VaultState(
    val entries: List<VaultEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val sortOrder: SortOrder = SortOrder.NAME_ASC
)

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModel(private val repository: VaultRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val sortOrder = MutableStateFlow(SortOrder.NAME_ASC)

    val state: StateFlow<VaultState> = combine(
        searchQuery,
        selectedCategory,
        sortOrder
    ) { query, category, sort ->
        Triple(query, category, sort)
    }.flatMapLatest { (query, category, sort) ->
        val flow = when {
            query.isNotBlank() -> repository.searchEntries(query)
            category != null -> repository.getEntriesByCategory(category)
            else -> repository.getAllEntries()
        }
        kotlinx.coroutines.flow.flow {
            flow.collect { entries ->
                val sorted = when (sort) {
                    SortOrder.NAME_ASC -> entries.sortedWith(
                        compareByDescending<VaultEntry> { it.isFavorite }
                            .thenBy { it.serviceName.lowercase() }
                    )
                    SortOrder.NAME_DESC -> entries.sortedWith(
                        compareByDescending<VaultEntry> { it.isFavorite }
                            .thenByDescending { it.serviceName.lowercase() }
                    )
                    SortOrder.RECENT -> entries.sortedWith(
                        compareByDescending<VaultEntry> { it.isFavorite }
                            .thenByDescending { it.updatedAt }
                    )
                    SortOrder.CREATED -> entries.sortedWith(
                        compareByDescending<VaultEntry> { it.isFavorite }
                            .thenByDescending { it.createdAt }
                    )
                }
                emit(
                    VaultState(
                        entries = sorted,
                        searchQuery = query,
                        selectedCategory = category,
                        sortOrder = sort
                    )
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaultState()
    )

    fun onSearchQuery(query: String) {
        searchQuery.update { query }
    }

    fun onCategorySelected(category: String?) {
        selectedCategory.update { category }
    }

    fun onSortOrderChanged(order: SortOrder) {
        sortOrder.update { order }
    }

    fun toggleFavorite(entry: VaultEntry) {
        viewModelScope.launch {
            repository.updateEntry(entry.copy(isFavorite = !entry.isFavorite))
        }
    }
}
