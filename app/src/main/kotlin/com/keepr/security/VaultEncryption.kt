package com.keepr.security

import android.util.Base64

class VaultEncryption(private val cryptoManager: CryptoManager) {

    fun encryptField(plaintext: String): String {
        if (plaintext.isEmpty()) return ""
        val key = cryptoManager.getOrCreateVaultKey()
        val encrypted = cryptoManager.encrypt(plaintext.toByteArray(Charsets.UTF_8), key)
        val combined = encrypted.iv + encrypted.ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decryptField(ciphertext: String): String {
        if (ciphertext.isEmpty()) return ""
        return try {
            val combined = Base64.decode(ciphertext, Base64.NO_WRAP)
            val iv = combined.copyOfRange(0, 12)
            val data = combined.copyOfRange(12, combined.size)
            val key = cryptoManager.getOrCreateVaultKey()
            cryptoManager.decrypt(EncryptedData(iv, data), key).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }

    fun encryptBytes(data: ByteArray): String {
        val key = cryptoManager.getOrCreateVaultKey()
        val encrypted = cryptoManager.encrypt(data, key)
        val combined = encrypted.iv + encrypted.ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decryptBytes(encoded: String): ByteArray? {
        return try {
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            val iv = combined.copyOfRange(0, 12)
            val data = combined.copyOfRange(12, combined.size)
            val key = cryptoManager.getOrCreateVaultKey()
            cryptoManager.decrypt(EncryptedData(iv, data), key)
        } catch (e: Exception) {
            null
        }
    }
}
