package com.keepr.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "keepr_prefs")

class PreferencesRepository(private val context: Context) {

    companion object {
        private val KEY_AUTO_LOCK = stringPreferencesKey("auto_lock_timeout")
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_SCREENSHOT_PROTECTION = booleanPreferencesKey("screenshot_protection")
        private val KEY_SORT_ORDER = stringPreferencesKey("sort_order")
        private val KEY_LAST_USED_CATEGORY = stringPreferencesKey("last_used_category")
        private val KEY_CLIPBOARD_CLEAR_DELAY = intPreferencesKey("clipboard_clear_delay")
    }

    val autoLockTimeout: Flow<AutoLockTimeout> = context.appDataStore.data.map { prefs ->
        AutoLockTimeout.fromString(prefs[KEY_AUTO_LOCK] ?: AutoLockTimeout.ONE_MINUTE.value)
    }

    val theme: Flow<AppTheme> = context.appDataStore.data.map { prefs ->
        AppTheme.fromString(prefs[KEY_THEME] ?: AppTheme.DARK.value)
    }

    val screenshotProtection: Flow<Boolean> = context.appDataStore.data.map { prefs ->
        prefs[KEY_SCREENSHOT_PROTECTION] != false
    }

    val sortOrder: Flow<SortOrder> = context.appDataStore.data.map { prefs ->
        SortOrder.fromString(prefs[KEY_SORT_ORDER] ?: SortOrder.NAME_ASC.value)
    }

    val clipboardClearDelay: Flow<Int> = context.appDataStore.data.map { prefs ->
        prefs[KEY_CLIPBOARD_CLEAR_DELAY] ?: 30
    }

    suspend fun setAutoLockTimeout(timeout: AutoLockTimeout) {
        context.appDataStore.edit { prefs ->
            prefs[KEY_AUTO_LOCK] = timeout.value
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.appDataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.value
        }
    }

    suspend fun setScreenshotProtection(enabled: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[KEY_SCREENSHOT_PROTECTION] = enabled
        }
    }

    suspend fun setSortOrder(order: SortOrder) {
        context.appDataStore.edit { prefs ->
            prefs[KEY_SORT_ORDER] = order.value
        }
    }

    suspend fun setClipboardClearDelay(seconds: Int) {
        context.appDataStore.edit { prefs ->
            prefs[KEY_CLIPBOARD_CLEAR_DELAY] = seconds
        }
    }
}

enum class AutoLockTimeout(val value: String, val displayName: String, val milliseconds: Long) {
    IMMEDIATELY("immediately", "Immediately", 0L),
    SECONDS_30("30s", "30 seconds", 30_000L),
    ONE_MINUTE("1m", "1 minute", 60_000L),
    FIVE_MINUTES("5m", "5 minutes", 300_000L),
    FIFTEEN_MINUTES("15m", "15 minutes", 900_000L),
    NEVER("never", "Never", Long.MAX_VALUE);

    companion object {
        fun fromString(value: String): AutoLockTimeout =
            entries.find { it.value == value } ?: ONE_MINUTE
    }
}

enum class AppTheme(val value: String, val displayName: String) {
    DARK("dark", "Dark"),
    SYSTEM("system", "System default");

    companion object {
        fun fromString(value: String): AppTheme =
            entries.find { it.value == value } ?: DARK
    }
}

enum class SortOrder(val value: String, val displayName: String) {
    NAME_ASC("name_asc", "Name A–Z"),
    NAME_DESC("name_desc", "Name Z–A"),
    RECENT("recent", "Recently updated"),
    CREATED("created", "Date added");

    companion object {
        fun fromString(value: String): SortOrder =
            entries.find { it.value == value } ?: NAME_ASC
    }
}
