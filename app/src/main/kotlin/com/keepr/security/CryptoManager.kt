package com.keepr.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val VAULT_KEY_ALIAS = "keepr_vault_key"
        private const val BIOMETRIC_KEY_ALIAS = "keepr_biometric_key"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_SIZE = 12
    }

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    fun getOrCreateVaultKey(): SecretKey {
        return if (keyStore.containsAlias(VAULT_KEY_ALIAS)) {
            (keyStore.getEntry(VAULT_KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            generateVaultKey()
        }
    }

    private fun generateVaultKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM, KEYSTORE_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            VAULT_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(PADDING)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun getOrCreateBiometricKey(): SecretKey {
        return if (keyStore.containsAlias(BIOMETRIC_KEY_ALIAS)) {
            (keyStore.getEntry(BIOMETRIC_KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            generateBiometricKey()
        }
    }

    private fun generateBiometricKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM, KEYSTORE_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            BIOMETRIC_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(PADDING)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun encrypt(plaintext: ByteArray, key: SecretKey): EncryptedData {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)
        return EncryptedData(iv, ciphertext)
    }

    fun decrypt(encryptedData: EncryptedData, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, encryptedData.iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(encryptedData.ciphertext)
    }

    fun getCipherForBiometricEncrypt(): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateBiometricKey())
        return cipher
    }

    fun getCipherForBiometricDecrypt(iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateBiometricKey(), spec)
        return cipher
    }

    fun deleteVaultKey() {
        if (keyStore.containsAlias(VAULT_KEY_ALIAS)) {
            keyStore.deleteEntry(VAULT_KEY_ALIAS)
        }
    }

    fun deleteBiometricKey() {
        if (keyStore.containsAlias(BIOMETRIC_KEY_ALIAS)) {
            keyStore.deleteEntry(BIOMETRIC_KEY_ALIAS)
        }
    }
}

data class EncryptedData(
    val iv: ByteArray,
    val ciphertext: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedData) return false
        return iv.contentEquals(other.iv) && ciphertext.contentEquals(other.ciphertext)
    }

    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + ciphertext.contentHashCode()
        return result
    }
}
