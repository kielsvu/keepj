package com.keepr

import android.app.Application
import com.keepr.data.database.VaultDatabase
import com.keepr.data.repository.PreferencesRepository
import com.keepr.data.repository.VaultRepository
import com.keepr.security.CryptoManager
import com.keepr.security.PinManager
import com.keepr.security.VaultEncryption
import com.keepr.utils.AutoLockManager
import com.keepr.utils.BackupManager

class KeeprApplication : Application() {

    val cryptoManager by lazy { CryptoManager() }
    val pinManager by lazy { PinManager(this) }
    val vaultEncryption by lazy { VaultEncryption(cryptoManager) }
    val database by lazy { VaultDatabase.getInstance(this) }
    val vaultRepository by lazy { VaultRepository(database.vaultDao(), vaultEncryption) }
    val preferencesRepository by lazy { PreferencesRepository(this) }
    val autoLockManager by lazy { AutoLockManager() }
    val backupManager by lazy { BackupManager(this, cryptoManager) }
}
