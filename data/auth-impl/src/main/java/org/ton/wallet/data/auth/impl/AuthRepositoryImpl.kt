package org.ton.wallet.data.auth.impl

import android.content.SharedPreferences
import android.util.Base64
import kotlinx.coroutines.flow.*
import org.ton.wallet.core.ext.clear
import org.ton.wallet.data.auth.api.AuthRepository
import org.ton.wallet.data.auth.api.PassCodeType
import org.ton.wallet.data.core.SecuredPrefsKeys
import org.ton.wallet.data.core.util.CoroutineScopes
import org.ton.wallet.lib.security.SecurityUtils
import kotlin.coroutines.*

class AuthRepositoryImpl(
    private val securedPreferences: SharedPreferences
) : AuthRepository {

    private val isBiometricAuthOnFlow = MutableStateFlow(false)
    private val isBiometricAvailableOnDeviceFlow = MutableStateFlow(false)

    override val hasPassCode: Boolean
        get() = securedPreferences.contains(SecuredPrefsKeys.PassCodeHash)

    override val isBiometricActiveFlow: StateFlow<Boolean> = combine(
        isBiometricAuthOnFlow,
        isBiometricAvailableOnDeviceFlow
    ) { isBiometricsAuthOn, isBiometricAvailable ->
        isBiometricsAuthOn && isBiometricAvailable
    }.stateIn(CoroutineScopes.repositoriesScope, SharingStarted.Eagerly, false)

    init {
        isBiometricAuthOnFlow.value = securedPreferences.getBoolean(SecuredPrefsKeys.BiometricTurnedOn, false)
    }

    override fun setBiometricAuthOn(hasBiometricAuth: Boolean) {
        isBiometricAuthOnFlow.value = hasBiometricAuth
        securedPreferences.edit()
            .putBoolean(SecuredPrefsKeys.BiometricTurnedOn, hasBiometricAuth)
            .apply()
    }

    override fun setBiometricAvailableOnDevice(isAvailable: Boolean) {
        isBiometricAvailableOnDeviceFlow.value = isAvailable
    }

    override suspend fun setPassCode(passCode: String, type: PassCodeType) {
        return suspendCoroutine { cont ->
            try {
                val salt = SecurityUtils.randomBytesSecured(16)
                val hash = SecurityUtils.getArgonHash(passCode.toByteArray(), salt)
                securedPreferences.edit()
                    .putString(SecuredPrefsKeys.PassCodeHash, Base64.encodeToString(hash, Base64.NO_WRAP))
                    .putString(SecuredPrefsKeys.PassCodeSalt, Base64.encodeToString(salt, Base64.NO_WRAP))
                    .putInt(SecuredPrefsKeys.PassCodeType, type.rawValue)
                    .apply()
                salt.clear()
                hash?.clear()
                cont.resume(Unit)
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }
    }

    override suspend fun checkPassCode(passCode: String): Boolean {
        return suspendCoroutine { cont ->
            try {
                val saltBase64 = securedPreferences.getString(SecuredPrefsKeys.PassCodeSalt, null)
                val salt: ByteArray = try {
                    Base64.decode(saltBase64, Base64.DEFAULT)
                } catch (e: Exception) {
                    throw IllegalStateException("Unable to decode ${SecuredPrefsKeys.PassCodeSalt}", e)
                }

                val hashToCheck = SecurityUtils.getArgonHash(passCode.toByteArray(), salt)
                salt.clear()

                val hashBase64 = securedPreferences.getString(SecuredPrefsKeys.PassCodeHash, null)
                val hash: ByteArray = try {
                    Base64.decode(hashBase64, Base64.DEFAULT)
                } catch (e: Exception) {
                    throw IllegalStateException("Unable to decode ${SecuredPrefsKeys.PassCodeHash}", e)
                }
                val isCorrect = hash.contentEquals(hashToCheck)
                hash.clear()
                cont.resume(isCorrect)
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }
    }

    override fun getPassCodeType(): PassCodeType? {
        return if (securedPreferences.contains(SecuredPrefsKeys.PassCodeType)) {
            val rawValue = securedPreferences.getInt(SecuredPrefsKeys.PassCodeType, 0)
            PassCodeType.fromRawValue(rawValue)
        } else {
            null
        }
    }

    override suspend fun deleteWallet() = Unit
}