package org.ton.wallet.feature.send.impl.connect

data class SendConnectConfirmState(
    val amount: Long,
    val receiverUfAddress: String = "",
    val senderUfAddress: String? = null,
    val feeString: String? = null,
    val payload: String? = null,
    val isSending: Boolean = false,
    val isSent: Boolean = false
)