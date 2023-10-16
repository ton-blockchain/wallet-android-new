package org.ton.wallet.data.core.link

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.ton.wallet.data.core.connect.TonConnectRequest

@Parcelize
sealed interface LinkAction : Parcelable {

    @Parcelize
    class TransferAction(
        val address: String?,
        val amount: Long?,
        val message: String?
    ) : LinkAction

    @Parcelize
    class TonConnectAction(
        val url: String,
        val version: Int,
        val clientId: String,
        val request: TonConnectRequest
    ) : LinkAction
}