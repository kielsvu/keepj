package com.keepr.ui.screens.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.keepr.ui.components.NumericKeypad
import com.keepr.ui.components.PinDots
import com.keepr.ui.theme.AccentPurple
import com.keepr.ui.theme.AccentPurpleContainer
import com.keepr.ui.theme.AccentPurpleLight
import com.keepr.ui.theme.Background
import com.keepr.ui.theme.ErrorColor
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.TextSecondary

@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onSetupComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.setupComplete) {
        if (state.setupComplete) onSetupComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
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
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = AccentPurpleLight,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Keepr",
                style = KeeprTypography.displayMedium
            )

            Spacer(Modifier.height(8.dp))

            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    (slideInHorizontally(tween(300)) { it / 3 } + fadeIn(tween(300))) togetherWith
                            (slideOutHorizontally(tween(200)) { -it / 3 } + fadeOut(tween(150)))
                },
                label = "setup_step"
            ) { step ->
                Text(
                    text = when (step) {
                        SetupStep.CREATE -> "Create a 6-digit PIN to secure your vault"
                        SetupStep.CONFIRM -> "Confirm your 6-digit PIN"
                    },
                    style = KeeprTypography.bodyMedium.copy(color = TextSecondary),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(48.dp))

            PinDots(
                pinLength = state.pin.length,
                maxLength = 6,
                hasError = state.error != null
            )

            Spacer(Modifier.height(16.dp))

            AnimatedContent(
                targetState = state.error,
                transitionSpec = {
                    fadeIn(tween(150)) togetherWith fadeOut(tween(100))
                },
                label = "error_text"
            ) { error ->
                if (error != null) {
                    Text(
                        text = error,
                        style = KeeprTypography.bodySmall.copy(color = ErrorColor),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Spacer(Modifier.height(KeeprTypography.bodySmall.fontSize.value.dp))
                }
            }

            Spacer(Modifier.weight(1f))

            NumericKeypad(
                onDigit = { viewModel.onDigit(it) },
                onDelete = { viewModel.onDelete() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
