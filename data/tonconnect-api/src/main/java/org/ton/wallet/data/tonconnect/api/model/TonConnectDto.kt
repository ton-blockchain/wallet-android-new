package org.ton.wallet.data.tonconnect.api.model

data class TonConnectDto(
    val accountId: Int,
    val clientId: String,
    val publicKey: String,
    val secretKey: String,
    val requestId: Int
)