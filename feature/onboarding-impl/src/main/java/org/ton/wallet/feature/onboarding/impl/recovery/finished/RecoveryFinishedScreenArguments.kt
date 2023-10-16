package org.ton.wallet.feature.onboarding.impl.recovery.finished

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.ScreenArguments

@Parcelize
class RecoveryFinishedScreenArguments(
    val isFromImport: Boolean
) : ScreenArguments {

    @IgnoredOnParcel
    override val screen: String = AppScreen.OnboardingRecoveryFinished.name
}