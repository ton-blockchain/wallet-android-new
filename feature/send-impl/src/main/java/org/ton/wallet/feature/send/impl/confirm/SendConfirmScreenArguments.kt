package org.ton.wallet.feature.send.impl.confirm

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.ScreenArguments

@Parcelize
class SendConfirmScreenArguments(
    val address: String,
    val amount: Long,
    val message: String?
) : ScreenArguments {

    @IgnoredOnParcel
    override val screen = AppScreen.SendConfirm.name
}