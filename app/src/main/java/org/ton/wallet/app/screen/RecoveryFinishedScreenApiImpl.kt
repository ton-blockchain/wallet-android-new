package org.ton.wallet.app.screen

import android.app.Activity
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.app.util.AppBiometricHelper
import org.ton.wallet.core.Res
import org.ton.wallet.feature.onboarding.api.RecoveryFinishedScreenApi
import org.ton.wallet.feature.onboarding.impl.finished.OnboardingFinishedScreenArguments
import org.ton.wallet.feature.passcode.impl.setup.PassCodeSetupScreenArguments
import org.ton.wallet.strings.RString

internal class RecoveryFinishedScreenApiImpl(
    private val navigator: Navigator
) : RecoveryFinishedScreenApi {

    override fun navigateToPassCodeCompleted(isImport: Boolean) {
        navigator.push(OnboardingFinishedScreenArguments(isImport), isRoot = true)
    }

    override fun navigateToPassCodeSetup(withBiometrics: Boolean) {
        val arguments = PassCodeSetupScreenArguments(
            isBackVisible = true,
            isDark = false,
            passCode = null,
            withBiometrics = withBiometrics
        )
        navigator.push(arguments)
    }

    override fun navigateToBiometricPrompt(activity: Activity, onSuccess: () -> Unit) {
        AppBiometricHelper.showBiometricPrompt(
            activity = activity as FragmentActivity,
            description = Res.str(RString.biometric_prompt_activate_description),
            callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess.invoke()
                }
            }
        )
    }
}