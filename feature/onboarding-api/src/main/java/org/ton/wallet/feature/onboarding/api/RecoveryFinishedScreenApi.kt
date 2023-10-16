package org.ton.wallet.feature.onboarding.api

import android.app.Activity

interface RecoveryFinishedScreenApi {

    fun navigateToPassCodeCompleted(isImport: Boolean)

    fun navigateToPassCodeSetup(withBiometrics: Boolean)

    fun navigateToBiometricPrompt(activity: Activity, onSuccess: () -> Unit)
}