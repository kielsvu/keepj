package com.keepr.ui.screens.lock

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.keepr.ui.components.NumericKeypad
import com.keepr.ui.components.PinDots
import com.keepr.ui.theme.AccentPurple
import com.keepr.ui.theme.AccentPurpleContainer
import com.keepr.ui.theme.AccentPurpleLight
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AccentPurpleContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LockOpen,
                    contentDescription = null,
                    tint = AccentPurpleLight,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Keepr",
                style = KeeprTypography.displaySmall
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Enter your PIN to continue",
                style = KeeprTypography.bodyMedium.copy(color = TextSecondary),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            PinDots(
                pinLength = state.pin.length,
                maxLength = 6,
                hasError = state.error != null
            )

            Spacer(Modifier.height(16.dp))

            AnimatedContent(
                targetState = state.error to state.lockedUntilText,
                transitionSpec = {
                    fadeIn(tween(150)) togetherWith fadeOut(tween(100))
                },
                label = "lock_msg"
            ) { (error, lockout) ->
                when {
                    error != null -> Text(
                        text = error,
                        style = KeeprTypography.bodySmall.copy(color = ErrorColor),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    lockout != null -> Text(
                        text = lockout,
                        style = KeeprTypography.bodySmall.copy(color = WarningColor),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    else -> Spacer(Modifier.height(KeeprTypography.bodySmall.fontSize.value.dp))
                }
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
                            modifier = Modifier.size(76.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Fingerprint,
                                contentDescription = "Unlock with biometrics",
                                tint = AccentPurple,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                } else null
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}
