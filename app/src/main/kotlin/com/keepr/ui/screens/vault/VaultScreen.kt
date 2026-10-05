package com.keepr.ui.screens.vault

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.animateItem
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.keepr.data.model.VaultCategory
import com.keepr.ui.components.ServiceIcon
import com.keepr.ui.components.VaultEntryItem
import com.keepr.ui.theme.AccentPurple
import com.keepr.ui.theme.AccentPurpleContainer
import com.keepr.ui.theme.AccentPurpleLight
import com.keepr.ui.theme.Background
import com.keepr.ui.theme.BorderDefault
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.SurfaceMid
import com.keepr.ui.theme.SurfaceRaised
import com.keepr.ui.theme.TextSecondary
import com.keepr.ui.theme.TextTertiary
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import com.keepr.ui.theme.TextPrimary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VaultScreen(
    viewModel: VaultViewModel,
    onEntryClick: (String) -> Unit,
    onAddEntry: () -> Unit,
    onSettings: () -> Unit,
    onLock: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Keepr",
                    style = KeeprTypography.headlineLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSettings) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onLock) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = "Lock vault",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            SearchBar(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(12.dp))

            CategoryFilter(
                selected = state.selectedCategory,
                onSelect = viewModel::onCategorySelected
            )

            AnimatedContent(
                targetState = state.entries.isEmpty(),
                label = "vault_content"
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyVaultState(
                        hasSearch = state.searchQuery.isNotEmpty() || state.selectedCategory != null,
                        onAdd = onAddEntry
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = 8.dp, bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val favorites = state.entries.filter { it.isFavorite }
                        val others = state.entries.filter { !it.isFavorite }

                        if (favorites.isNotEmpty()) {
                            stickyHeader {
                                SectionHeader(
                                    title = "Favorites",
                                    modifier = Modifier.background(Background)
                                )
                            }
                            items(favorites, key = { it.id }) { entry ->
                                VaultEntryItem(
                                    entry = entry,
                                    onClick = { onEntryClick(entry.id) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(entry) },
                                    modifier = Modifier
                                )
                            }
                        }

                        if (others.isNotEmpty()) {
                            if (favorites.isNotEmpty()) {
                                stickyHeader {
                                    SectionHeader(
                                        title = "All accounts",
                                        modifier = Modifier.background(Background)
                                    )
                                }
                            }
                            items(others, key = { it.id }) { entry ->
                                VaultEntryItem(
                                    entry = entry,
                                    onClick = { onEntryClick(entry.id) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(entry) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddEntry,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            shape = CircleShape,
            containerColor = AccentPurple
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = "Add account",
                tint = TextPrimary
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceMid)
            .border(1.dp, BorderDefault, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = KeeprTypography.bodyMedium.copy(color = TextPrimary),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(AccentPurple),
                singleLine = true,
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            "Search accounts...",
                            style = KeeprTypography.bodyMedium.copy(color = TextTertiary)
                        )
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun CategoryFilter(
    selected: String?,
    onSelect: (String?) -> Unit
) {
    val categories = listOf(null) + VaultCategory.entries.map { it.displayName }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(44.dp)
    ) {
        items(categories) { category ->
            val isSelected = selected == category
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) AccentPurpleContainer else SurfaceMid)
                    .border(
                        1.dp,
                        if (isSelected) AccentPurple.copy(0.5f) else BorderDefault,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(category) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category ?: "All",
                    style = KeeprTypography.labelMedium.copy(
                        color = if (isSelected) AccentPurpleLight else TextSecondary
                    )
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = KeeprTypography.labelMedium,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun EmptyVaultState(
    hasSearch: Boolean,
    onAdd: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hasSearch) {
            Text(
                text = "No results",
                style = KeeprTypography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Try a different search or category",
                style = KeeprTypography.bodyMedium.copy(color = TextSecondary),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Nothing here yet",
                style = KeeprTypography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Add your first account to get started",
                style = KeeprTypography.bodyMedium.copy(color = TextSecondary),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            com.keepr.ui.components.KeeprPrimaryButton(
                text = "Add account",
                onClick = onAdd,
                modifier = Modifier.width(200.dp)
            )
        }
    }
}
