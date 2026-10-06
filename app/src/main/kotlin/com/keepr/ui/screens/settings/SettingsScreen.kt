package com.keepr.ui.screens.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.ScreenshotMonitor
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import com.keepr.BuildConfig
import com.keepr.data.repository.AutoLockTimeout
import com.keepr.ui.components.ConfirmDialog
import com.keepr.ui.theme.AccentPurple
import com.keepr.ui.theme.AccentPurpleContainer
import com.keepr.ui.theme.AccentPurpleLight
import com.keepr.ui.theme.Background
import com.keepr.ui.theme.BorderDefault
import com.keepr.ui.theme.BorderSubtle
import com.keepr.ui.theme.ErrorColor
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.SurfaceMid
import com.keepr.ui.theme.TextPrimary
import com.keepr.ui.theme.TextSecondary
import com.keepr.ui.theme.TextTertiary

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

    var autoLockExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                    }
                    Text(
                        "Settings",
                        style = KeeprTypography.titleLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // SECURITY section
            item {
                SectionGroup(label = "SECURITY") {
                    SettingsRow(
                        icon = Icons.Outlined.Lock,
                        label = "Change PIN",
                        onClick = onChangePin
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    ToggleRow(
                        icon = Icons.Outlined.Fingerprint,
                        label = "Biometric Unlock",
                        checked = state.biometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric(context, it) }
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    Box {
                        SettingsRow(
                            icon = Icons.Outlined.Timer,
                            label = "Auto-Lock",
                            value = state.autoLockTimeout.displayName,
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
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    ToggleRow(
                        icon = Icons.Outlined.ScreenshotMonitor,
                        label = "Block Screenshots",
                        checked = state.screenshotProtection,
                        onCheckedChange = { viewModel.setScreenshotProtection(it) }
                    )
                }
            }

            // VAULT section
            item {
                SectionGroup(label = "VAULT") {
                    SettingsRow(
                        icon = Icons.Outlined.FileUpload,
                        label = "Export Vault",
                        onClick = {
                            exportLauncher.launch("keepr_backup_${System.currentTimeMillis()}.kbk")
                        }
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    SettingsRow(
                        icon = Icons.Outlined.FileDownload,
                        label = "Import Vault",
                        onClick = { importLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    DestructiveRow(
                        icon = Icons.Outlined.DeleteForever,
                        label = "Clear Vault",
                        onClick = { viewModel.showClearConfirm() }
                    )
                }
            }

            // ABOUT section
            item {
                SectionGroup(label = "ABOUT") {
                    SettingsRow(
                        icon = Icons.Outlined.Info,
                        label = "Version",
                        value = try { BuildConfig.VERSION_NAME } catch (e: Exception) { "1.0.0" },
                        showChevron = false,
                        onClick = {}
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    SettingsRow(
                        icon = Icons.Outlined.PrivacyTip,
                        label = "Privacy Policy",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://keepr.app/privacy"))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        if (state.showClearConfirm) {
            ConfirmDialog(
                title = "Clear Vault",
                message = "This will permanently delete all accounts. This cannot be undone.",
                confirmText = "Clear",
                destructive = true,
                onConfirm = { viewModel.clearVault() },
                onDismiss = { viewModel.hideClearConfirm() }
            )
        }
    }
}

@Composable
private fun SectionGroup(
    label: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = KeeprTypography.labelSmall,
            color = TextTertiary
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceMid)
                .border(1.dp, BorderDefault, RoundedCornerShape(16.dp))
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String? = null,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "row_scale"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AccentPurpleContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentPurpleLight, modifier = Modifier.size(20.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(label, style = KeeprTypography.bodyMedium, color = TextPrimary)
            if (value != null) {
                Text(value, style = KeeprTypography.bodySmall, color = TextSecondary)
            }
        }
        if (showChevron) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AccentPurpleContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentPurpleLight, modifier = Modifier.size(20.dp))
        }
        Text(
            label,
            style = KeeprTypography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
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
private fun DestructiveRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "destructive_row_scale"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ErrorColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ErrorColor, modifier = Modifier.size(20.dp))
        }
        Text(
            label,
            style = KeeprTypography.bodyMedium,
            color = ErrorColor,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
