package org.ton.wallet.feature.send.impl.connect

data class SendConnectConfirmState(
    val amount: Long,
    val receiver: String,
    val feeString: String?,
    val isSending: Boolean,
    val isSent: Boolean
)