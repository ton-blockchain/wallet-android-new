package org.ton.wallet.feature.send.impl.connect

import org.ton.wallet.data.core.model.MessageData

data class SendConnectConfirmState(
    val requestMessages: List<MessageData> = emptyList(),
    val senderUfAddress: String? = null,
    val feeString: String? = null,
    val payload: String? = null,
    val isSending: Boolean = false,
    val isSent: Boolean = false
) {
    // TODO: remove usage of amount and receiverUfAddress
    @Deprecated("Remove usage of amount")
    val amount: Long = 0

    @Deprecated("Remove usage of receiverUfAddress")
    val receiverUfAddress: String = ""
}