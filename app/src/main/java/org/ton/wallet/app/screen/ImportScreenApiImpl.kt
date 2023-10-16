package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.feature.onboarding.api.ImportScreenApi
import org.ton.wallet.feature.onboarding.impl.recovery.finished.RecoveryFinishedScreenArguments
import org.ton.wallet.screen.AppScreen

internal class ImportScreenApiImpl(
    private val navigator: Navigator
) : ImportScreenApi {

    override fun navigateToRecoveryChecked() {
        navigator.push(RecoveryFinishedScreenArguments(true), isRoot = true)
    }

    override fun navigateToNoPhrase() {
        navigator.push(AppScreen.OnboardingNoPhrase.name)
    }
}