package org.ton.wallet.app.screen

import android.app.Activity
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.app.util.AppBiometricHelper
import org.ton.wallet.feature.passcode.api.PassCodeEnterScreenApi

internal class PassCodeEnterScreenApiImpl(
    private val navigator: Navigator
) : PassCodeEnterScreenApi {

    override fun navigateBack() {
        navigator.pop(false)
    }

    override fun showBiometricPrompt(activity: Activity, description: String, onSuccess: () -> Unit) {
        AppBiometricHelper.showBiometricPrompt(
            activity = activity as FragmentActivity,
            description = description,
            callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess.invoke()
                }
            }
        )
    }
}