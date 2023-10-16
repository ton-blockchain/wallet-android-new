package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.feature.onboarding.api.CongratulationsScreenApi
import org.ton.wallet.feature.onboarding.impl.recovery.show.RecoveryShowScreenArguments

internal class CongratulationsScreenApiImpl(
    private val navigator: Navigator
) : CongratulationsScreenApi {

    override fun navigateToShowRecovery() {
        navigator.push(RecoveryShowScreenArguments(isOnlyShow = false))
    }
}