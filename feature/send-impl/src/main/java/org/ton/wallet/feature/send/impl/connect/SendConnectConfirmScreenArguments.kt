package org.ton.wallet.feature.send.impl.connect

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.data.tonconnect.api.model.TonConnectEvent
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.ScreenArguments

@Parcelize
class SendConnectConfirmScreenArguments(
    val transfer: TonConnectEvent.Transfer
) : ScreenArguments {

    @IgnoredOnParcel
    override val screen = AppScreen.SendConnect.name
}