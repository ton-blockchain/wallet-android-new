package org.ton.wallet.data.auth.api

import kotlinx.coroutines.flow.StateFlow
import org.ton.wallet.data.core.BaseRepository

interface AuthRepository : BaseRepository {

    val hasPassCode: Boolean

    val isBiometricActiveFlow: StateFlow<Boolean>

    fun setBiometricAuthOn(hasBiometricAuth: Boolean)

    fun setBiometricAvailableOnDevice(isAvailable: Boolean)

    suspend fun setPassCode(passCode: String, type: PassCodeType)

    suspend fun checkPassCode(passCode: String): Boolean

    fun getPassCodeType(): PassCodeType?
}