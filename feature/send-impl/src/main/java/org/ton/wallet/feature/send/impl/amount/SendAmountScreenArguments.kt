package org.ton.wallet.feature.send.impl.amount

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.ScreenArguments

@Parcelize
class SendAmountScreenArguments(
    val ufAddress: String,
    val dns: String? = null,
    val amount: Long? = null,
    val message: String? = null,
    val isAddressEditable: Boolean = true
) : ScreenArguments {

    @IgnoredOnParcel
    override val screen = AppScreen.SendAmount.name
}