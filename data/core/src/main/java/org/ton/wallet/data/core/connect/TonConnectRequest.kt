package org.ton.wallet.data.core.connect

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class TonConnectRequest(
    val manifestUrl: String,
    val items: List<ConnectItem>
) : Parcelable {

    @Parcelize
    sealed interface ConnectItem : Parcelable {

        @Parcelize
        data object Address : ConnectItem

        @Parcelize
        data class Proof(val payload: String?) : ConnectItem

        @Parcelize
        data class UnknownMethod(val method: String) : ConnectItem
    }
}