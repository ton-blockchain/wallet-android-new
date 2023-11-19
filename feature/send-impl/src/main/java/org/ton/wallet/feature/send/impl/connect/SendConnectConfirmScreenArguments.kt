package org.ton.wallet.feature.send.impl.connect

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.lib.tonconnect.TonConnectEvent
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.ScreenArguments

@Parcelize
class SendConnectConfirmScreenArguments(
    val event: TonConnectEvent
) : ScreenArguments {

    @IgnoredOnParcel
    override val screen = AppScreen.SendConnect.name
}