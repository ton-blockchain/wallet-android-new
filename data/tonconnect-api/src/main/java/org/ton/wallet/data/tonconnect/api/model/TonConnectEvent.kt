package org.ton.wallet.data.tonconnect.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class TonConnectEvent {

    @Parcelize
    class Transfer(
        val clientId: String,
        val requestId: Int,
        val rawAddress: String,
        val amount: Long,
        val stateInit: ByteArray?
    ) : TonConnectEvent(), Parcelable
}