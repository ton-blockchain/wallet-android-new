package org.ton.wallet.feature.onboarding.impl.recovery.finished

import android.app.Activity
import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.ton.wallet.data.auth.api.AuthRepository
import org.ton.wallet.feature.onboarding.api.RecoveryFinishedScreenApi
import org.ton.wallet.feature.passcode.api.PassCodeSetupScreenApi
import org.ton.wallet.lib.security.BiometricUtils
import org.ton.wallet.screen.viewmodel.BaseViewModel

class RecoveryFinishedViewModel(
    private val arguments: RecoveryFinishedScreenArguments
) : BaseViewModel() {

    private val authRepository: AuthRepository by inject()
    private val context: Context by inject()
    private val screenApi: RecoveryFinishedScreenApi by inject()

    private val _isBiometricCheckedFlow = MutableStateFlow(true)
    val isBiometricCheckedFlow: Flow<Boolean> = _isBiometricCheckedFlow

    override fun onResultReceived(code: String, args: Bundle?) {
        super.onResultReceived(code, args)
        if (code == PassCodeSetupScreenApi.ResultCodePassCodeSet) {
            authRepository.setBiometricAuthOn(_isBiometricCheckedFlow.value)
            screenApi.navigateToPassCodeCompleted(arguments.isFromImport)
        }
    }

    fun onDoneClicked(activity: Activity) {
        val isBiometricChecked = _isBiometricCheckedFlow.value
        if (isBiometricChecked && BiometricUtils.isBiometricsAvailableOnDevice(context)) {
            screenApi.navigateToBiometricPrompt(activity) {
                screenApi.navigateToPassCodeSetup(_isBiometricCheckedFlow.value)
            }
        } else {
            screenApi.navigateToPassCodeSetup(_isBiometricCheckedFlow.value)
        }
    }

    fun onBiometricCheckChanged(isChecked: Boolean) {
        _isBiometricCheckedFlow.value = isChecked
    }
}