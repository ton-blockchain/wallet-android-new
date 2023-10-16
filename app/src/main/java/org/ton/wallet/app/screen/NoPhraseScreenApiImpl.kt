package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.feature.onboarding.api.NoPhraseScreenApi
import org.ton.wallet.screen.AppScreen

internal class NoPhraseScreenApiImpl(
    private val navigator: Navigator
) : NoPhraseScreenApi {

    override fun navigateBack() {
        navigator.pop(false)
    }

    override fun navigateToCongratulations() {
        navigator.push(AppScreen.OnboardingCongratulations.name, isRoot = true)
    }
}