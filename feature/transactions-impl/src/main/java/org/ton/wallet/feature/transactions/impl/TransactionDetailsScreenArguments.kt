package org.ton.wallet.feature.transactions.impl

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.ScreenArguments

@Parcelize
class TransactionDetailsScreenArguments(
    val internalId: Long
) : ScreenArguments {

    @IgnoredOnParcel
    override val screen: String = AppScreen.TransactionDetails.name
}