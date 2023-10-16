package org.ton.wallet.feature.onboarding.impl.recovery.show

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.ScreenArguments

@Parcelize
class RecoveryShowScreenArguments(
    val isOnlyShow: Boolean,
    val popToScreenName: String? = null
) : ScreenArguments {

    @IgnoredOnParcel
    override val screen: String = AppScreen.OnboardingRecoveryShow.name
}