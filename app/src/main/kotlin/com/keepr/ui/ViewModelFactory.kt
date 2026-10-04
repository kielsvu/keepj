package com.keepr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.keepr.KeeprApplication
import com.keepr.ui.screens.detail.DetailViewModel
import com.keepr.ui.screens.form.AccountFormViewModel
import com.keepr.ui.screens.generator.GeneratorViewModel
import com.keepr.ui.screens.lock.LockViewModel
import com.keepr.ui.screens.settings.ChangePinViewModel
import com.keepr.ui.screens.settings.SettingsViewModel
import com.keepr.ui.screens.setup.SetupViewModel
import com.keepr.ui.screens.vault.VaultViewModel

class KeeprViewModelFactory(
    private val app: KeeprApplication,
    private val entryId: String? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(SetupViewModel::class.java) ->
            SetupViewModel(app.pinManager) as T

        modelClass.isAssignableFrom(LockViewModel::class.java) ->
            LockViewModel(app.pinManager, app.cryptoManager, app.autoLockManager) as T

        modelClass.isAssignableFrom(VaultViewModel::class.java) ->
            VaultViewModel(app.vaultRepository) as T

        modelClass.isAssignableFrom(DetailViewModel::class.java) ->
            DetailViewModel(app.vaultRepository, entryId!!) as T

        modelClass.isAssignableFrom(AccountFormViewModel::class.java) ->
            AccountFormViewModel(app.vaultRepository, entryId) as T

        modelClass.isAssignableFrom(GeneratorViewModel::class.java) ->
            GeneratorViewModel() as T

        modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
            SettingsViewModel(
                app.pinManager,
                app.cryptoManager,
                app.preferencesRepository,
                app.vaultRepository,
                app.backupManager,
                app.autoLockManager
            ) as T

        modelClass.isAssignableFrom(ChangePinViewModel::class.java) ->
            ChangePinViewModel(app.pinManager) as T

        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
