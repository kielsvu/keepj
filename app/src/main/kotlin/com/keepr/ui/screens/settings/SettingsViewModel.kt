package com.keepr.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepr.data.repository.AutoLockTimeout
import com.keepr.data.repository.PreferencesRepository
import com.keepr.data.repository.VaultRepository
import com.keepr.security.CryptoManager
import com.keepr.security.PinManager
import com.keepr.utils.AutoLockManager
import com.keepr.utils.BackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val biometricEnabled: Boolean = false,
    val autoLockTimeout: AutoLockTimeout = AutoLockTimeout.ONE_MINUTE,
    val screenshotProtection: Boolean = true,
    val showClearConfirm: Boolean = false,
    val message: String? = null
)

class SettingsViewModel(
    private val pinManager: PinManager,
    private val cryptoManager: CryptoManager,
    private val preferencesRepository: PreferencesRepository,
    private val vaultRepository: VaultRepository,
    private val backupManager: BackupManager,
    private val autoLockManager: AutoLockManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val biometric = pinManager.isBiometricEnabled.first()
            val autoLock = preferencesRepository.autoLockTimeout.first()
            val screenshot = preferencesRepository.screenshotProtection.first()
            _state.update {
                it.copy(
                    biometricEnabled = biometric,
                    autoLockTimeout = autoLock,
                    screenshotProtection = screenshot
                )
            }
        }
    }

    fun toggleBiometric(context: Context, enable: Boolean) {
        if (!enable) {
            viewModelScope.launch {
                pinManager.disableBiometric()
                cryptoManager.deleteBiometricKey()
                _state.update { it.copy(biometricEnabled = false) }
            }
            return
        }

        val activity = context as? FragmentActivity ?: return
        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            _state.update { it.copy(message = "Biometric authentication not available") }
            return
        }

        try {
            val cipher = cryptoManager.getCipherForBiometricEncrypt()
            val executor = ContextCompat.getMainExecutor(activity)

            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val authenticatedCipher = result.cryptoObject?.cipher ?: return
                    viewModelScope.launch {
                        val token = "keepr_biometric_token".toByteArray()
                        val encrypted = authenticatedCipher.doFinal(token)
                        val iv = cipher.iv
                        pinManager.storeBiometricToken(encrypted, iv)
                        _state.update { it.copy(biometricEnabled = true, message = "Biometric unlock enabled") }
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    _state.update { it.copy(message = "Setup failed: $errString") }
                }

                override fun onAuthenticationFailed() {
                    _state.update { it.copy(message = "Authentication failed") }
                }
            }

            val prompt = BiometricPrompt(activity, executor, callback)
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Enable biometric unlock")
                .setSubtitle("Confirm your identity to enable biometrics")
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()

            prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
        } catch (e: Exception) {
            _state.update { it.copy(message = "Biometric setup failed") }
        }
    }

    fun setAutoLock(timeout: AutoLockTimeout) {
        viewModelScope.launch {
            preferencesRepository.setAutoLockTimeout(timeout)
            autoLockManager.updateTimeout(timeout)
            _state.update { it.copy(autoLockTimeout = timeout) }
        }
    }

    fun setScreenshotProtection(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setScreenshotProtection(enabled)
            _state.update { it.copy(screenshotProtection = enabled) }
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                val entries = mutableListOf<com.keepr.data.model.VaultEntry>()
                vaultRepository.getAllEntries().first { list ->
                    entries.addAll(list)
                    true
                }
                val backupBytes = backupManager.createBackup(entries)
                backupManager.writeBackupToUri(uri, backupBytes)
                _state.update { it.copy(message = "Backup exported (${entries.size} accounts)") }
            } catch (e: Exception) {
                _state.update { it.copy(message = "Export failed: ${e.message}") }
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.restoreBackup(uri)
            result.fold(
                onSuccess = { entries ->
                    vaultRepository.importEntries(entries)
                    _state.update { it.copy(message = "Restored ${entries.size} accounts") }
                },
                onFailure = { e ->
                    _state.update { it.copy(message = "Import failed: ${e.message}") }
                }
            )
        }
    }

    fun showClearConfirm() = _state.update { it.copy(showClearConfirm = true) }
    fun hideClearConfirm() = _state.update { it.copy(showClearConfirm = false) }

    fun clearVault() {
        viewModelScope.launch {
            vaultRepository.deleteAllEntries()
            _state.update { it.copy(showClearConfirm = false, message = "Vault cleared") }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}
