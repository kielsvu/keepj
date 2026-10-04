package com.keepr.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import android.util.Base64

private val Context.pinDataStore: DataStore<Preferences> by preferencesDataStore(name = "keepr_pin_store")

class PinManager(private val context: Context) {

    companion object {
        private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        private val KEY_PIN_SALT = stringPreferencesKey("pin_salt")
        private val KEY_PIN_SET = booleanPreferencesKey("pin_set")
        private val KEY_FAILED_ATTEMPTS = intPreferencesKey("failed_attempts")
        private val KEY_LOCK_UNTIL = longPreferencesKey("lock_until")
        private val KEY_BIOMETRIC_ENCRYPTED = stringPreferencesKey("biometric_encrypted")
        private val KEY_BIOMETRIC_IV = stringPreferencesKey("biometric_iv")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")

        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 200_000
        private const val KEY_LENGTH = 256
        private const val SALT_SIZE = 32
        private const val MAX_ATTEMPTS = 5
        private const val BASE_DELAY_MS = 30_000L
    }

    val isPinSet: Flow<Boolean> = context.pinDataStore.data.map { prefs ->
        prefs[KEY_PIN_SET] == true
    }

    val isBiometricEnabled: Flow<Boolean> = context.pinDataStore.data.map { prefs ->
        prefs[KEY_BIOMETRIC_ENABLED] == true
    }

    val failedAttempts: Flow<Int> = context.pinDataStore.data.map { prefs ->
        prefs[KEY_FAILED_ATTEMPTS] ?: 0
    }

    val lockUntil: Flow<Long> = context.pinDataStore.data.map { prefs ->
        prefs[KEY_LOCK_UNTIL] ?: 0L
    }

    suspend fun setPin(pin: String) {
        val salt = generateSalt()
        val hash = deriveKey(pin, salt)
        context.pinDataStore.edit { prefs ->
            prefs[KEY_PIN_HASH] = Base64.encodeToString(hash, Base64.NO_WRAP)
            prefs[KEY_PIN_SALT] = Base64.encodeToString(salt, Base64.NO_WRAP)
            prefs[KEY_PIN_SET] = true
            prefs[KEY_FAILED_ATTEMPTS] = 0
            prefs[KEY_LOCK_UNTIL] = 0L
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val prefs = context.pinDataStore.data.first()
        val lockUntil = prefs[KEY_LOCK_UNTIL] ?: 0L
        if (System.currentTimeMillis() < lockUntil) return false

        val storedHash = Base64.decode(prefs[KEY_PIN_HASH] ?: return false, Base64.NO_WRAP)
        val salt = Base64.decode(prefs[KEY_PIN_SALT] ?: return false, Base64.NO_WRAP)
        val inputHash = deriveKey(pin, salt)

        return if (constantTimeEquals(storedHash, inputHash)) {
            resetFailedAttempts()
            true
        } else {
            incrementFailedAttempts()
            false
        }
    }

    suspend fun isLockedOut(): Boolean {
        val prefs = context.pinDataStore.data.first()
        val lockUntil = prefs[KEY_LOCK_UNTIL] ?: 0L
        return System.currentTimeMillis() < lockUntil
    }

    suspend fun getLockRemainingMs(): Long {
        val prefs = context.pinDataStore.data.first()
        val lockUntil = prefs[KEY_LOCK_UNTIL] ?: 0L
        return maxOf(0L, lockUntil - System.currentTimeMillis())
    }

    private suspend fun incrementFailedAttempts() {
        context.pinDataStore.edit { prefs ->
            val attempts = (prefs[KEY_FAILED_ATTEMPTS] ?: 0) + 1
            prefs[KEY_FAILED_ATTEMPTS] = attempts
            if (attempts >= MAX_ATTEMPTS) {
                val lockMultiplier = maxOf(1, attempts - MAX_ATTEMPTS + 1)
                val lockDuration = BASE_DELAY_MS * lockMultiplier
                prefs[KEY_LOCK_UNTIL] = System.currentTimeMillis() + lockDuration
            }
        }
    }

    private suspend fun resetFailedAttempts() {
        context.pinDataStore.edit { prefs ->
            prefs[KEY_FAILED_ATTEMPTS] = 0
            prefs[KEY_LOCK_UNTIL] = 0L
        }
    }

    suspend fun clearPin() {
        context.pinDataStore.edit { prefs ->
            prefs.remove(KEY_PIN_HASH)
            prefs.remove(KEY_PIN_SALT)
            prefs.remove(KEY_PIN_SET)
            prefs.remove(KEY_FAILED_ATTEMPTS)
            prefs.remove(KEY_LOCK_UNTIL)
            prefs.remove(KEY_BIOMETRIC_ENCRYPTED)
            prefs.remove(KEY_BIOMETRIC_IV)
            prefs[KEY_BIOMETRIC_ENABLED] = false
        }
    }

    suspend fun storeBiometricToken(encryptedToken: ByteArray, iv: ByteArray) {
        context.pinDataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_ENCRYPTED] = Base64.encodeToString(encryptedToken, Base64.NO_WRAP)
            prefs[KEY_BIOMETRIC_IV] = Base64.encodeToString(iv, Base64.NO_WRAP)
            prefs[KEY_BIOMETRIC_ENABLED] = true
        }
    }

    suspend fun getBiometricToken(): Pair<ByteArray, ByteArray>? {
        val prefs = context.pinDataStore.data.first()
        val encrypted = prefs[KEY_BIOMETRIC_ENCRYPTED] ?: return null
        val iv = prefs[KEY_BIOMETRIC_IV] ?: return null
        return Pair(
            Base64.decode(encrypted, Base64.NO_WRAP),
            Base64.decode(iv, Base64.NO_WRAP)
        )
    }

    suspend fun disableBiometric() {
        context.pinDataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_ENABLED] = false
            prefs.remove(KEY_BIOMETRIC_ENCRYPTED)
            prefs.remove(KEY_BIOMETRIC_IV)
        }
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_SIZE)
        random.nextBytes(salt)
        return salt
    }

    private fun deriveKey(pin: String, salt: ByteArray): ByteArray {
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val spec: KeySpec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        return factory.generateSecret(spec).encoded
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}
