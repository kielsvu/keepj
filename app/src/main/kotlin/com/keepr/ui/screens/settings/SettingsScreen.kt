package com.keepr.ui.screens.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import com.keepr.data.repository.AutoLockTimeout
import com.keepr.ui.components.ConfirmDialog
import com.keepr.ui.theme.AccentPurple
import com.keepr.ui.theme.AccentPurpleContainer
import com.keepr.ui.theme.Background
import com.keepr.ui.theme.BorderDefault
import com.keepr.ui.theme.ErrorColor
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.SurfaceMid
import com.keepr.ui.theme.TextSecondary
import com.keepr.ui.theme.TextTertiary
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember as rememberState
import androidx.compose.runtime.setValue

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onChangePin: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { viewModel.exportBackup(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBackup(it) }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    var autoLockExpanded by rememberState { mutableStateOf(false) }

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
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                }
                Text(
                    "Settings",
                    style = KeeprTypography.titleLarge,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionLabel("Security")

                SettingsRow(
                    title = "Change PIN",
                    subtitle = "Update your master PIN",
                    onClick = onChangePin
                )

                ToggleSettingsRow(
                    title = "Biometric unlock",
                    subtitle = "Use fingerprint or face to unlock",
                    checked = state.biometricEnabled,
                    onCheckedChange = { viewModel.toggleBiometric(context, it) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Fingerprint, null, tint = AccentPurple, modifier = Modifier.size(20.dp))
                    }
                )

                Box {
                    SettingsRow(
                        title = "Auto-lock",
                        subtitle = state.autoLockTimeout.displayName,
                        onClick = { autoLockExpanded = true }
                    )
                    DropdownMenu(
                        expanded = autoLockExpanded,
                        onDismissRequest = { autoLockExpanded = false }
                    ) {
                        AutoLockTimeout.entries.forEach { timeout ->
                            DropdownMenuItem(
                                text = { Text(timeout.displayName) },
                                onClick = {
                                    viewModel.setAutoLock(timeout)
                                    autoLockExpanded = false
                                }
                            )
                        }
                    }
                }

                ToggleSettingsRow(
                    title = "Screenshot protection",
                    subtitle = "Block screenshots in app switcher",
                    checked = state.screenshotProtection,
                    onCheckedChange = { viewModel.setScreenshotProtection(it) }
                )

                Spacer(Modifier.height(8.dp))
                SectionLabel("Vault")

                SettingsRow(
                    title = "Export backup",
                    subtitle = "Save encrypted backup to device",
                    onClick = {
                        exportLauncher.launch("keepr_backup_${System.currentTimeMillis()}.kbk")
                    }
                )

                SettingsRow(
                    title = "Import backup",
                    subtitle = "Restore from a backup file",
                    onClick = { importLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                )

                SettingsRow(
                    title = "Clear vault",
                    subtitle = "Delete all accounts",
                    titleColor = ErrorColor,
                    onClick = { viewModel.showClearConfirm() }
                )

                Spacer(Modifier.height(8.dp))
                SectionLabel("About")

                SettingsInfoRow(title = "Version", value = "1.0.0")
                SettingsInfoRow(title = "Privacy", value = "Offline, no tracking")

                Spacer(Modifier.height(24.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        if (state.showClearConfirm) {
            ConfirmDialog(
                title = "Clear vault?",
                message = "This permanently deletes all accounts. This cannot be undone.",
                confirmText = "Clear vault",
                destructive = true,
                onConfirm = { viewModel.clearVault() },
                onDismiss = { viewModel.hideClearConfirm() }
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = KeeprTypography.labelMedium.copy(color = AccentPurple),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = com.keepr.ui.theme.TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceMid)
            .border(1.dp, BorderDefault, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = KeeprTypography.bodyMedium.copy(color = titleColor))
            if (subtitle != null) {
                Text(subtitle, style = KeeprTypography.bodySmall)
            }
        }
        Icon(
            Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ToggleSettingsRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceMid)
            .border(1.dp, BorderDefault, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.size(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = KeeprTypography.bodyMedium)
            if (subtitle != null) {
                Text(subtitle, style = KeeprTypography.bodySmall)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentPurple,
                checkedTrackColor = AccentPurpleContainer
            )
        )
    }
}

@Composable
private fun SettingsInfoRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceMid)
            .border(1.dp, BorderDefault, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = KeeprTypography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = KeeprTypography.bodyMedium.copy(color = TextSecondary))
    }
}
