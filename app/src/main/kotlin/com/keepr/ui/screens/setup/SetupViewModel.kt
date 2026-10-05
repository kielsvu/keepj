package com.keepr.ui.screens.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepr.security.PinManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SetupStep { CREATE, CONFIRM }

data class SetupState(
    val step: SetupStep = SetupStep.CREATE,
    val pin: String = "",
    val firstPin: String = "",
    val error: String? = null,
    val setupComplete: Boolean = false
)

class SetupViewModel(private val pinManager: PinManager) : ViewModel() {

    private val _state = MutableStateFlow(SetupState())
    val state: StateFlow<SetupState> = _state.asStateFlow()

    private val pinLength = 6

    fun onDigit(digit: String) {
        val current = _state.value
        if (current.pin.length >= pinLength) return

        val newPin = current.pin + digit
        _state.update { it.copy(pin = newPin, error = null) }

        if (newPin.length == pinLength) {
            when (current.step) {
                SetupStep.CREATE -> advanceToConfirm()
                SetupStep.CONFIRM -> confirmPin(newPin)
            }
        }
    }

    fun onDelete() {
        _state.update { it.copy(pin = it.pin.dropLast(1), error = null) }
    }

    private fun advanceToConfirm() {
        _state.update { current ->
            current.copy(
                step = SetupStep.CONFIRM,
                firstPin = current.pin,
                pin = ""
            )
        }
    }

    private fun confirmPin(confirmedPin: String) {
        val firstPin = _state.value.firstPin
        if (confirmedPin != firstPin) {
            _state.update { it.copy(pin = "", error = "PINs don't match. Try again.") }
            return
        }
        viewModelScope.launch {
            pinManager.setPin(confirmedPin)
            _state.update { it.copy(setupComplete = true) }
        }
    }
}
