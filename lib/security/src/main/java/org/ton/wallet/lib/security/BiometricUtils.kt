package org.ton.wallet.lib.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

object BiometricUtils {

    const val Authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG

    private const val EncryptionAlgorithm = KeyProperties.KEY_ALGORITHM_AES
    private const val EncryptionBlockMode = KeyProperties.BLOCK_MODE_GCM
    private const val EncryptionPadding = KeyProperties.ENCRYPTION_PADDING_NONE

    fun isBiometricsAvailableOnDevice(context: Context): Boolean {
        val authStatus = BiometricManager.from(context).canAuthenticate(Authenticators)
        return authStatus == BiometricManager.BIOMETRIC_SUCCESS || authStatus == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }

    fun isBiometricsNoneEnrolled(context: Context): Boolean {
        return BiometricManager.from(context).canAuthenticate(Authenticators) == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }

    fun getBiometricCipher(keyStore: KeyStore, keyStoreName: String, keyAlias: String): Cipher {
        val cipher = Cipher.getInstance("${EncryptionAlgorithm}/${EncryptionBlockMode}/${EncryptionPadding}")
        var secretKey = keyStore.getKey(keyAlias, null)
        if (secretKey == null) {
            val purpose = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(keyAlias, purpose).apply {
                setBlockModes(EncryptionBlockMode)
                setEncryptionPaddings(EncryptionPadding)
                setUserAuthenticationRequired(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setInvalidatedByBiometricEnrollment(true)
                }
            }.build()
            val keyGenerator = KeyGenerator.getInstance(EncryptionAlgorithm, keyStoreName)
            keyGenerator.init(keyGenParameterSpec)
            secretKey = keyGenerator.generateKey()
        }
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }
}