package com.keepr.ui.screens.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepr.security.PinManager
import com.keepr.ui.components.NumericKeypad
import com.keepr.ui.components.PinDots
import com.keepr.ui.theme.Background
import com.keepr.ui.theme.ErrorColor
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.TextSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ChangePinStep { CURRENT, NEW, CONFIRM }

data class ChangePinState(
    val step: ChangePinStep = ChangePinStep.CURRENT,
    val pin: String = "",
    val newPin: String = "",
    val error: String? = null,
    val done: Boolean = false
)

class ChangePinViewModel(private val pinManager: PinManager) : ViewModel() {
    private val _state = MutableStateFlow(ChangePinState())
    val state: StateFlow<ChangePinState> = _state.asStateFlow()

    fun onDigit(digit: String) {
        val s = _state.value
        if (s.pin.length >= 12) return
        val newPin = s.pin + digit
        _state.update { it.copy(pin = newPin, error = null) }

        when (s.step) {
            ChangePinStep.CURRENT -> if (newPin.length >= 6) verifyCurrent(newPin)
            ChangePinStep.NEW -> if (newPin.length == 12) advanceToConfirm()
            ChangePinStep.CONFIRM -> if (newPin.length == s.newPin.length) confirmNew(newPin)
        }
    }

    fun onDelete() = _state.update { it.copy(pin = it.pin.dropLast(1), error = null) }

    private fun verifyCurrent(pin: String) {
        viewModelScope.launch {
            val valid = pinManager.verifyPin(pin)
            if (valid) {
                _state.update { it.copy(step = ChangePinStep.NEW, pin = "") }
            } else {
                _state.update { it.copy(pin = "", error = "Incorrect PIN") }
            }
        }
    }

    private fun advanceToConfirm() {
        _state.update { it.copy(step = ChangePinStep.CONFIRM, newPin = it.pin, pin = "") }
    }

    private fun confirmNew(confirmed: String) {
        val s = _state.value
        if (confirmed != s.newPin) {
            _state.update { it.copy(pin = "", error = "PINs don't match") }
            return
        }
        viewModelScope.launch {
            pinManager.setPin(confirmed)
            _state.update { it.copy(done = true) }
        }
    }
}

@Composable
fun ChangePinScreen(
    viewModel: ChangePinViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.done) {
        if (state.done) onDone()
    }

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
                Text("Change PIN", style = KeeprTypography.titleLarge, modifier = Modifier.padding(start = 4.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.weight(1f))

                AnimatedContent(
                    targetState = state.step,
                    transitionSpec = {
                        (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                    },
                    label = "change_pin_step"
                ) { step ->
                    Text(
                        text = when (step) {
                            ChangePinStep.CURRENT -> "Enter your current PIN"
                            ChangePinStep.NEW -> "Enter your new PIN"
                            ChangePinStep.CONFIRM -> "Confirm new PIN"
                        },
                        style = KeeprTypography.bodyMedium.copy(color = TextSecondary),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(32.dp))

                PinDots(
                    pinLength = state.pin.length,
                    maxLength = 12,
                    hasError = state.error != null
                )

                if (state.error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = state.error!!,
                        style = KeeprTypography.bodySmall.copy(color = ErrorColor),
                        textAlign = TextAlign.Center
                    )
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
}
