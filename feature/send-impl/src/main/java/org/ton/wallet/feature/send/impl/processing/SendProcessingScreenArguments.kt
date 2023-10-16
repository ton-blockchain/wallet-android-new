package org.ton.wallet.feature.send.impl.processing

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.ScreenArguments

@Parcelize
class SendProcessingScreenArguments(
    val address: String,
    val amount: Long,
    val fee: Long,
    val message: String?
) : ScreenArguments {

    @IgnoredOnParcel
    override val screen = AppScreen.SendProcessing.name
}