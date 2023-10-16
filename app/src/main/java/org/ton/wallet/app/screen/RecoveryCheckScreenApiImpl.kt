package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.feature.onboarding.api.RecoveryCheckScreenApi
import org.ton.wallet.feature.onboarding.impl.recovery.finished.RecoveryFinishedScreenArguments

internal class RecoveryCheckScreenApiImpl(
    private val navigator: Navigator
) : RecoveryCheckScreenApi {

    override fun navigateToRecoveryFinished() {
        navigator.push(RecoveryFinishedScreenArguments(isFromImport = false))
    }

    override fun navigateBack() {
        navigator.pop(false)
    }
}