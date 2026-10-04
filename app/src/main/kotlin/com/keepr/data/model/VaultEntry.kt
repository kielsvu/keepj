package com.keepr.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "vault_entries")
data class VaultEntry(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "service_name")
    val serviceName: String,

    @ColumnInfo(name = "account_label")
    val accountLabel: String = "",

    @ColumnInfo(name = "username")
    val username: String = "",

    @ColumnInfo(name = "email")
    val email: String = "",

    @ColumnInfo(name = "password_encrypted")
    val passwordEncrypted: String = "",

    @ColumnInfo(name = "website")
    val website: String = "",

    @ColumnInfo(name = "category")
    val category: String = "Other",

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

enum class VaultCategory(val displayName: String) {
    SOCIAL("Social"),
    EMAIL("Email"),
    BANKING("Banking"),
    SHOPPING("Shopping"),
    ENTERTAINMENT("Entertainment"),
    WORK("Work"),
    DEVELOPMENT("Development"),
    UTILITIES("Utilities"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): VaultCategory =
            entries.find { it.displayName == value } ?: OTHER
    }
}
