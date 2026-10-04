package com.keepr.ui.screens.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.keepr.ui.components.KeeprPrimaryButton
import com.keepr.ui.components.NumericKeypad
import com.keepr.ui.components.PinDots
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
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(0.5f))

            Text(
                text = "Keepr",
                style = KeeprTypography.displayMedium
            )

            Spacer(Modifier.height(8.dp))

            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                },
                label = "setup_step"
            ) { step ->
                Text(
                    text = when (step) {
                        SetupStep.CREATE -> "Create a master PIN to secure your vault"
                        SetupStep.CONFIRM -> "Confirm your PIN"
                    },
                    style = KeeprTypography.bodyMedium.copy(color = TextSecondary),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(Modifier.height(40.dp))

            PinDots(
                pinLength = state.pin.length,
                maxLength = state.pin.length.coerceAtLeast(6).coerceAtMost(12),
                hasError = state.error != null
            )

            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.error!!,
                    style = KeeprTypography.bodySmall.copy(color = com.keepr.ui.theme.ErrorColor)
                )
            }

            Spacer(Modifier.weight(0.5f))

            NumericKeypad(
                onDigit = { viewModel.onDigit(it) },
                onDelete = { viewModel.onDelete() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
