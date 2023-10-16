package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.feature.onboarding.api.StartScreenApi
import org.ton.wallet.screen.AppScreen

internal class StartScreenApiImpl(
    private val navigator: Navigator
) : StartScreenApi {

    override fun navigateToCongratulations() {
        navigator.push(AppScreen.OnboardingCongratulations.name, isRoot = true)
    }

    override fun navigateToImport() {
        navigator.push(AppScreen.OnboardingImport.name)
    }
}