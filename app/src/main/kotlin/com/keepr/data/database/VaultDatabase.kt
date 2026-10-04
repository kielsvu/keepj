package com.keepr.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.keepr.data.model.VaultEntry

@Database(
    entities = [VaultEntry::class],
    version = 1,
    exportSchema = true
)
abstract class VaultDatabase : RoomDatabase() {

    abstract fun vaultDao(): VaultDao

    companion object {
        private const val DATABASE_NAME = "keepr_vault.db"

        @Volatile
        private var instance: VaultDatabase? = null

        fun getInstance(context: Context): VaultDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): VaultDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                VaultDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
