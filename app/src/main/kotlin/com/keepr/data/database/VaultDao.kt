package com.keepr.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.keepr.data.model.VaultEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Query("SELECT * FROM vault_entries ORDER BY is_favorite DESC, service_name ASC, account_label ASC")
    fun getAllEntries(): Flow<List<VaultEntry>>

    @Query("SELECT * FROM vault_entries WHERE is_favorite = 1 ORDER BY service_name ASC, account_label ASC")
    fun getFavoriteEntries(): Flow<List<VaultEntry>>

    @Query("""
        SELECT * FROM vault_entries
        WHERE lower(service_name) LIKE '%' || lower(:query) || '%'
           OR lower(account_label) LIKE '%' || lower(:query) || '%'
           OR lower(username) LIKE '%' || lower(:query) || '%'
           OR lower(email) LIKE '%' || lower(:query) || '%'
           OR lower(website) LIKE '%' || lower(:query) || '%'
           OR lower(category) LIKE '%' || lower(:query) || '%'
           OR lower(notes) LIKE '%' || lower(:query) || '%'
        ORDER BY is_favorite DESC, service_name ASC, account_label ASC
    """)
    fun searchEntries(query: String): Flow<List<VaultEntry>>

    @Query("SELECT * FROM vault_entries WHERE category = :category ORDER BY is_favorite DESC, service_name ASC")
    fun getEntriesByCategory(category: String): Flow<List<VaultEntry>>

    @Query("SELECT * FROM vault_entries WHERE id = :id")
    suspend fun getEntryById(id: String): VaultEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: VaultEntry)

    @Update
    suspend fun updateEntry(entry: VaultEntry)

    @Delete
    suspend fun deleteEntry(entry: VaultEntry)

    @Query("DELETE FROM vault_entries")
    suspend fun deleteAllEntries()

    @Query("SELECT COUNT(*) FROM vault_entries")
    suspend fun getEntryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<VaultEntry>)
}
