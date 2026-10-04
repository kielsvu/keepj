package com.keepr.ui.screens.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.keepr.ui.components.ConfirmDialog
import com.keepr.ui.components.ServiceIcon
import com.keepr.ui.theme.AccentPurple
import com.keepr.ui.theme.AccentPurpleContainer
import com.keepr.ui.theme.AccentPurpleLight
import com.keepr.ui.theme.Background
import com.keepr.ui.theme.BorderDefault
import com.keepr.ui.theme.FavoriteActive
import com.keepr.ui.theme.FavoriteInactive
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.SurfaceMid
import com.keepr.ui.theme.TextSecondary
import com.keepr.ui.theme.TextTertiary
import com.keepr.utils.ClipboardUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.deleted) {
        if (state.deleted) onDeleted()
    }

    LaunchedEffect(state.copiedField) {
        state.copiedField?.let {
            snackbarHostState.showSnackbar("$it copied")
            viewModel.clearCopiedField()
        }
    }

    val entry = state.entry ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary
                    )
                }
                Spacer(Modifier.weight(1f))

                val favColor by animateColorAsState(
                    targetValue = if (entry.isFavorite) FavoriteActive else FavoriteInactive,
                    animationSpec = tween(200),
                    label = "fav_color"
                )
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        if (entry.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (entry.isFavorite) "Remove favorite" else "Add favorite",
                        tint = favColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = { onEdit(entry.id) }) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { viewModel.showDeleteConfirm() }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = com.keepr.ui.theme.ErrorColor, modifier = Modifier.size(22.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ServiceIcon(
                    serviceName = entry.serviceName,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(text = entry.serviceName, style = KeeprTypography.headlineMedium)
                if (entry.accountLabel.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = entry.accountLabel,
                        style = KeeprTypography.bodyMedium.copy(color = AccentPurpleLight)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = entry.category,
                    style = KeeprTypography.labelMedium.copy(color = TextTertiary)
                )
            }

            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (entry.username.isNotEmpty()) {
                    FieldRow(
                        label = "Username",
                        value = entry.username,
                        onCopy = {
                            ClipboardUtils.copyToClipboard(context, "Username", entry.username)
                            viewModel.onFieldCopied("Username")
                        }
                    )
                }
                if (entry.email.isNotEmpty()) {
                    FieldRow(
                        label = "Email",
                        value = entry.email,
                        onCopy = {
                            ClipboardUtils.copyToClipboard(context, "Email", entry.email)
                            viewModel.onFieldCopied("Email")
                        }
                    )
                }
                if (entry.passwordEncrypted.isNotEmpty()) {
                    PasswordFieldRow(
                        password = entry.passwordEncrypted,
                        visible = state.passwordVisible,
                        onToggleVisibility = { viewModel.togglePasswordVisibility() },
                        onCopy = {
                            ClipboardUtils.copyToClipboard(context, "Password", entry.passwordEncrypted)
                            viewModel.onFieldCopied("Password")
                        }
                    )
                }
                if (entry.website.isNotEmpty()) {
                    FieldRow(
                        label = "Website",
                        value = entry.website,
                        onCopy = {
                            ClipboardUtils.copyToClipboard(context, "Website", entry.website)
                            viewModel.onFieldCopied("Website")
                        }
                    )
                }
                if (entry.notes.isNotEmpty()) {
                    NotesRow(notes = entry.notes)
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                Text(
                    "Added ${fmt.format(Date(entry.createdAt))}",
                    style = KeeprTypography.labelSmall
                )
                if (entry.updatedAt != entry.createdAt) {
                    Text(
                        "Updated ${fmt.format(Date(entry.updatedAt))}",
                        style = KeeprTypography.labelSmall
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        if (state.showDeleteConfirm) {
            ConfirmDialog(
                title = "Delete account?",
                message = "This will permanently remove ${entry.serviceName}${if (entry.accountLabel.isNotEmpty()) " (${entry.accountLabel})" else ""} from your vault.",
                confirmText = "Delete",
                destructive = true,
                onConfirm = { viewModel.deleteEntry() },
                onDismiss = { viewModel.hideDeleteConfirm() }
            )
        }
    }
}

@Composable
private fun FieldRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceMid)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = KeeprTypography.labelSmall)
            Spacer(Modifier.height(2.dp))
            Text(value, style = KeeprTypography.bodyMedium, maxLines = 2)
        }
        IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy $label", tint = TextTertiary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PasswordFieldRow(
    password: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceMid)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Password", style = KeeprTypography.labelSmall)
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (visible) password else "•".repeat(password.length.coerceAtMost(16)),
                style = KeeprTypography.bodyMedium.copy(
                    fontFamily = if (visible) FontFamily.Monospace else FontFamily.Default
                ),
                maxLines = 2
            )
        }
        IconButton(onClick = onToggleVisibility, modifier = Modifier.size(36.dp)) {
            Icon(
                if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                contentDescription = if (visible) "Hide password" else "Show password",
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
        IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy password", tint = TextTertiary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun NotesRow(notes: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceMid)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text("Notes", style = KeeprTypography.labelSmall)
        Spacer(Modifier.height(4.dp))
        Text(notes, style = KeeprTypography.bodyMedium)
    }
}
