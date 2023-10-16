package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.feature.onboarding.api.RecoveryShowScreenApi
import org.ton.wallet.screen.AppScreen

internal class RecoveryShowScreenApiImpl(
    private val navigator: Navigator
) : RecoveryShowScreenApi {

    override fun navigateToCheck() {
        navigator.push(AppScreen.OnboardingRecoveryCheck.name)
    }

    override fun navigateBack() {
        navigator.pop(false)
    }

    override fun popToScreenKeepCurrent(screenName: String) {
        navigator.popTo(screenName, true)
    }
}