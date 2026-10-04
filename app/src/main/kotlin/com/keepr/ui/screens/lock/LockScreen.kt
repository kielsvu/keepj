package com.keepr.ui.screens.lock

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.keepr.ui.components.NumericKeypad
import com.keepr.ui.components.PinDots
import com.keepr.ui.theme.AccentPurple
import com.keepr.ui.theme.Background
import com.keepr.ui.theme.ErrorColor
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.TextSecondary
import com.keepr.ui.theme.WarningColor

@Composable
fun LockScreen(
    viewModel: LockViewModel,
    onUnlocked: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.unlocked) {
        if (state.unlocked) onUnlocked()
    }

    LaunchedEffect(state.showBiometricPrompt) {
        if (state.showBiometricPrompt) {
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            viewModel.launchBiometricPrompt(activity)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            Text(
                text = "Keepr",
                style = KeeprTypography.displaySmall
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Your vault is locked",
                style = KeeprTypography.bodyMedium.copy(color = TextSecondary)
            )

            Spacer(Modifier.height(48.dp))

            PinDots(
                pinLength = state.pin.length,
                maxLength = 12,
                hasError = state.error != null
            )

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(150))
            ) {
                Text(
                    text = state.error ?: "",
                    style = KeeprTypography.bodySmall.copy(color = ErrorColor),
                    textAlign = TextAlign.Center
                )
            }

            AnimatedVisibility(
                visible = state.lockedUntilText != null,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(150))
            ) {
                Text(
                    text = state.lockedUntilText ?: "",
                    style = KeeprTypography.bodySmall.copy(color = WarningColor),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.weight(1f))

            NumericKeypad(
                onDigit = { if (!state.isLockedOut) viewModel.onDigit(it) },
                onDelete = { viewModel.onDelete() },
                modifier = Modifier.fillMaxWidth(),
                extraAction = if (state.biometricEnabled) {
                    {
                        IconButton(
                            onClick = { viewModel.triggerBiometric() },
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Fingerprint,
                                contentDescription = "Unlock with biometrics",
                                tint = AccentPurple,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                } else null
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
