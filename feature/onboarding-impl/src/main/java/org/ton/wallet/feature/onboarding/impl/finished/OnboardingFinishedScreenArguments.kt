package org.ton.wallet.feature.onboarding.impl.finished

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.ScreenArguments

@Parcelize
class OnboardingFinishedScreenArguments(
    val isImport: Boolean
) : ScreenArguments, Parcelable {

    @IgnoredOnParcel
    override val screen: String = AppScreen.OnboardingFinished.name
}