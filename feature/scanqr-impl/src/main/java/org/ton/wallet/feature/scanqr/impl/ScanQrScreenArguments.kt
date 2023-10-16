package org.ton.wallet.feature.scanqr.impl

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.ScreenArguments

@Parcelize
class ScanQrScreenArguments(
    val linkActionTypes: Array<Int>
) : ScreenArguments {

    @IgnoredOnParcel
    override val screen = AppScreen.ScanQr.name

    companion object {

        const val LinkActionTypeTransfer = 0
        const val LinkActionTypeTonConnect = 1
    }
}