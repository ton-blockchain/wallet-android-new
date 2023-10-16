package org.ton.wallet.feature.send.impl.address

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.ScreenArguments

@Parcelize
class SendAddressScreenArguments(
    val address: String? = null,
    val amount: Long? = null,
    val message: String? = null
) : ScreenArguments {

    @IgnoredOnParcel
    override val screen: String = AppScreen.SendAddress.name
}