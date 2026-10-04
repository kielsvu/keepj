package com.keepr.data.repository

import com.keepr.data.database.VaultDao
import com.keepr.data.model.VaultEntry
import com.keepr.security.VaultEncryption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VaultRepository(
    private val vaultDao: VaultDao,
    private val vaultEncryption: VaultEncryption
) {

    fun getAllEntries(): Flow<List<VaultEntry>> =
        vaultDao.getAllEntries().map { entries -> entries.map { decryptEntry(it) } }

    fun searchEntries(query: String): Flow<List<VaultEntry>> =
        vaultDao.searchEntries(query).map { entries -> entries.map { decryptEntry(it) } }

    fun getEntriesByCategory(category: String): Flow<List<VaultEntry>> =
        vaultDao.getEntriesByCategory(category).map { entries -> entries.map { decryptEntry(it) } }

    suspend fun getEntryById(id: String): VaultEntry? =
        vaultDao.getEntryById(id)?.let { decryptEntry(it) }

    suspend fun insertEntry(entry: VaultEntry) {
        vaultDao.insertEntry(encryptEntry(entry))
    }

    suspend fun updateEntry(entry: VaultEntry) {
        vaultDao.updateEntry(encryptEntry(entry.copy(updatedAt = System.currentTimeMillis())))
    }

    suspend fun deleteEntry(entry: VaultEntry) {
        vaultDao.deleteEntry(entry)
    }

    suspend fun deleteAllEntries() {
        vaultDao.deleteAllEntries()
    }

    suspend fun getEntryCount(): Int = vaultDao.getEntryCount()

    suspend fun getRawEntries(): List<VaultEntry> {
        var result = emptyList<VaultEntry>()
        vaultDao.getAllEntries().collect { result = it }
        return result
    }

    suspend fun importEntries(entries: List<VaultEntry>) {
        val encrypted = entries.map { encryptEntry(it) }
        vaultDao.insertAll(encrypted)
    }

    private fun encryptEntry(entry: VaultEntry): VaultEntry {
        return entry.copy(
            serviceName = entry.serviceName,
            accountLabel = entry.accountLabel,
            username = if (entry.username.isEmpty()) "" else vaultEncryption.encryptField(entry.username),
            email = if (entry.email.isEmpty()) "" else vaultEncryption.encryptField(entry.email),
            passwordEncrypted = if (entry.passwordEncrypted.isEmpty()) "" else vaultEncryption.encryptField(entry.passwordEncrypted),
            website = if (entry.website.isEmpty()) "" else vaultEncryption.encryptField(entry.website),
            notes = if (entry.notes.isEmpty()) "" else vaultEncryption.encryptField(entry.notes)
        )
    }

    private fun decryptEntry(entry: VaultEntry): VaultEntry {
        return entry.copy(
            username = if (entry.username.isEmpty()) "" else vaultEncryption.decryptField(entry.username),
            email = if (entry.email.isEmpty()) "" else vaultEncryption.decryptField(entry.email),
            passwordEncrypted = if (entry.passwordEncrypted.isEmpty()) "" else vaultEncryption.decryptField(entry.passwordEncrypted),
            website = if (entry.website.isEmpty()) "" else vaultEncryption.decryptField(entry.website),
            notes = if (entry.notes.isEmpty()) "" else vaultEncryption.decryptField(entry.notes)
        )
    }
}
