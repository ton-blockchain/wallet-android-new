package org.ton.wallet.feature.tonconnect.impl

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.ScreenArguments

@Parcelize
class TonConnectApproveScreenArguments(
    val url: String
) : ScreenArguments {

    @IgnoredOnParcel
    override val screen = AppScreen.TonConnectApprove.name
}