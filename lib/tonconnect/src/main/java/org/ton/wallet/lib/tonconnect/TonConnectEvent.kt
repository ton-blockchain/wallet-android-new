package org.ton.wallet.lib.tonconnect

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class TonConnectEvent(
    val clientId: String,
    val eventId: Long,
    val request: TonConnectApi.AppRequestEvent
) : Parcelable