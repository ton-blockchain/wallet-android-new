package org.ton.wallet.app.screen

import android.app.Activity
import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.app.util.AppBiometricHelper
import org.ton.wallet.app.util.AppIntentUtils
import org.ton.wallet.core.Res
import org.ton.wallet.feature.onboarding.impl.recovery.show.RecoveryShowScreenArguments
import org.ton.wallet.feature.passcode.impl.enter.PassCodeEnterScreenArguments
import org.ton.wallet.feature.passcode.impl.setup.PassCodeSetupScreenArguments
import org.ton.wallet.feature.settings.api.SettingsScreenApi
import org.ton.wallet.lib.log.L
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.strings.RString

internal class SettingsScreenApiImpl(
    private val navigator: Navigator
) : SettingsScreenApi {

    override fun navigateToRecovery() {
        navigator.push(RecoveryShowScreenArguments(
            isOnlyShow = true,
            popToScreenName = AppScreen.Settings.name
        ))
    }

    override fun navigateChangePassCode() {
        val arguments = PassCodeSetupScreenArguments(
            isBackVisible = true,
            withBiometrics = false
        )
        navigator.push(arguments)
    }

    override fun navigateBack() {
        navigator.pop(false)
    }

    override fun navigateToSettings() {
        navigator.popTo(AppScreen.Settings.name)
    }

    override fun navigateToPassCodeEnter(purpose: String, withBiometrics: Boolean) {
        navigator.push(PassCodeEnterScreenArguments(purpose = purpose, isOnlyPassCode = !withBiometrics))
    }

    override fun navigateToStart() {
        navigator.push(AppScreen.OnboardingStart.name, isReplace = true, isRoot = true)
    }

    override fun showAppNotificationsSettings(context: Context, channelId: String) {
        AppIntentUtils.openAppNotificationsSettings(context, channelId)
    }

    override fun showBiometricEnrollAlert(context: Context) {
        AppBiometricHelper.showBiometricEnrollAlert(context)
    }

    override fun showBiometricPrompt(activity: Activity, onSuccess: () -> Unit) {
        AppBiometricHelper.showBiometricPrompt(
            activity = activity as FragmentActivity,
            description = Res.str(RString.biometric_prompt_default_description),
            callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess.invoke()
                }
            }
        )
    }

    override fun onPermissionRequested() {
        AppIntentUtils.isAppIntentStarted = true
    }

    override fun shareLogs(activity: Activity) {
        val file = L.getArchive()
        AppIntentUtils.shareFile(
            context = activity,
            file = file,
            chooserTitle = "Share logs",
            contentType = "application/zip"
        )
    }
}