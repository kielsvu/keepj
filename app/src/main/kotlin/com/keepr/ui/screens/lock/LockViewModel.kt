package com.keepr.ui.screens.lock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepr.security.CryptoManager
import com.keepr.security.PinManager
import com.keepr.utils.AutoLockManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LockState(
    val pin: String = "",
    val error: String? = null,
    val unlocked: Boolean = false,
    val biometricEnabled: Boolean = false,
    val showBiometricPrompt: Boolean = false,
    val isLockedOut: Boolean = false,
    val lockedUntilText: String? = null
)

class LockViewModel(
    private val pinManager: PinManager,
    private val cryptoManager: CryptoManager,
    private val autoLockManager: AutoLockManager
) : ViewModel() {

    private val _state = MutableStateFlow(LockState())
    val state: StateFlow<LockState> = _state.asStateFlow()

    private var lockoutJob: Job? = null

    init {
        viewModelScope.launch {
            val biometricEnabled = pinManager.isBiometricEnabled.first()
            val isLockedOut = pinManager.isLockedOut()
            _state.update { it.copy(biometricEnabled = biometricEnabled, isLockedOut = isLockedOut) }
            if (isLockedOut) startLockoutTimer()
            if (biometricEnabled && !isLockedOut) {
                _state.update { it.copy(showBiometricPrompt = true) }
            }
        }
    }

    fun onDigit(digit: String) {
        if (_state.value.isLockedOut) return
        val newPin = _state.value.pin + digit
        _state.update { it.copy(pin = newPin, error = null) }
        if (newPin.length >= 6) {
            attemptPinUnlock(newPin)
        }
    }

    fun onDelete() {
        _state.update { it.copy(pin = it.pin.dropLast(1), error = null) }
    }

    private fun attemptPinUnlock(pin: String) {
        viewModelScope.launch {
            val success = pinManager.verifyPin(pin)
            if (success) {
                autoLockManager.unlock()
                _state.update { it.copy(unlocked = true) }
            } else {
                val isLockedOut = pinManager.isLockedOut()
                val error = if (isLockedOut) {
                    startLockoutTimer()
                    "Too many attempts"
                } else {
                    val remaining = pinManager.failedAttempts.first()
                    val attemptsLeft = (5 - remaining).coerceAtLeast(0)
                    if (attemptsLeft > 0) "Incorrect PIN ($attemptsLeft attempts left)" else "Incorrect PIN"
                }
                _state.update { it.copy(pin = "", error = error, isLockedOut = isLockedOut) }
            }
        }
    }

    fun triggerBiometric() {
        _state.update { it.copy(showBiometricPrompt = true) }
    }

    fun launchBiometricPrompt(activity: FragmentActivity) {
        _state.update { it.copy(showBiometricPrompt = false) }

        viewModelScope.launch {
            val tokenData = pinManager.getBiometricToken() ?: return@launch
            val (_, iv) = tokenData

            try {
                val cipher = cryptoManager.getCipherForBiometricDecrypt(iv)
                val executor = ContextCompat.getMainExecutor(activity)

                val callback = object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        autoLockManager.unlock()
                        _state.update { it.copy(unlocked = true) }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                            errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                        ) {
                            _state.update { it.copy(error = "Biometric failed. Use your PIN.") }
                        }
                    }

                    override fun onAuthenticationFailed() {
                        _state.update { it.copy(error = "Not recognized. Try again.") }
                    }
                }

                val prompt = BiometricPrompt(activity, executor, callback)
                val info = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock Keepr")
                    .setSubtitle("Use your biometric to access your vault")
                    .setNegativeButtonText("Use PIN")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build()

                prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
            } catch (e: Exception) {
                _state.update { it.copy(error = "Biometric unavailable. Use your PIN.") }
            }
        }
    }

    private fun startLockoutTimer() {
        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            while (true) {
                val remaining = pinManager.getLockRemainingMs()
                if (remaining <= 0) {
                    _state.update { it.copy(isLockedOut = false, lockedUntilText = null) }
                    break
                }
                val minutes = remaining / 60000
                val seconds = (remaining % 60000) / 1000
                val text = if (minutes > 0) {
                    "Try again in ${minutes}m ${seconds}s"
                } else {
                    "Try again in ${seconds}s"
                }
                _state.update { it.copy(lockedUntilText = text) }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        lockoutJob?.cancel()
        super.onCleared()
    }
}
