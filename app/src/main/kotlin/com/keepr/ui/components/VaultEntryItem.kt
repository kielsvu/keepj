package com.keepr.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keepr.data.model.VaultEntry
import com.keepr.ui.theme.AccentPurple
import com.keepr.ui.theme.AccentPurpleContainer
import com.keepr.ui.theme.AccentPurpleLight
import com.keepr.ui.theme.FavoriteActive
import com.keepr.ui.theme.FavoriteInactive
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.SurfaceMid
import com.keepr.ui.theme.TextSecondary
import com.keepr.ui.theme.TextTertiary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VaultEntryItem(
    entry: VaultEntry,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteColor by animateColorAsState(
        targetValue = if (entry.isFavorite) FavoriteActive else FavoriteInactive,
        animationSpec = tween(200),
        label = "favorite_color"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ServiceIcon(
            serviceName = entry.serviceName,
            modifier = Modifier.size(44.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.serviceName,
                    style = KeeprTypography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (entry.accountLabel.isNotEmpty()) {
                    Text(
                        text = " · ${entry.accountLabel}",
                        style = KeeprTypography.titleSmall,
                        color = AccentPurpleLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            val subtitle = when {
                entry.email.isNotEmpty() -> entry.email
                entry.username.isNotEmpty() -> entry.username
                else -> entry.category
            }

            Text(
                text = subtitle,
                style = KeeprTypography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier
                .size(36.dp)
                .semantics { contentDescription = if (entry.isFavorite) "Remove from favorites" else "Add to favorites" }
        ) {
            Icon(
                imageVector = if (entry.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = favoriteColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ServiceIcon(
    serviceName: String,
    modifier: Modifier = Modifier
) {
    val initials = serviceName
        .trim()
        .split(" ")
        .take(2)
        .joinToString("") { it.firstOrNull()?.uppercaseChar()?.toString() ?: "" }
        .take(2)
        .ifEmpty { "?" }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AccentPurpleContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = KeeprTypography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = AccentPurpleLight
            )
        )
    }
}
