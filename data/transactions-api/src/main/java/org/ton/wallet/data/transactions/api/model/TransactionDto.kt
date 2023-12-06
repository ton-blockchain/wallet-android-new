package org.ton.wallet.data.transactions.api.model

class TransactionDto(
    var internalId: Long = 0,
    val hash: String,
    val accountId: Int,
    val status: TransactionStatus,
    val timestampSec: Long?,
    val inMessage: TransactionMessageDto?,
    val outMessages: List<TransactionMessageDto>?,
    val fee: Long? = null,
    val storageFee: Long? = null,
    val validUntilSec: Long? = null,
    val inMsgBodyHash: String? = null,
) {
    // TODO: remove usage of amount, peerAddress, message, type
    @Deprecated("Remove usage of amount")
    val amount: Long?
        get() = outMessages?.firstOrNull()?.amount ?: inMessage?.amount

    @Deprecated("Remove usage of peerAddress")
    val peerAddress: String?
        get() = outMessages?.firstOrNull()?.address ?: inMessage?.address

    @Deprecated("Remove usage of message")
    val message: String?
        get() = outMessages?.firstOrNull()?.message ?: inMessage?.message

    @Deprecated("Remove usage of type")
    val type: Type =
        if (outMessages?.isNotEmpty() == true) Type.Out
        else if (inMessage != null) Type.In
        else Type.Unknown

    enum class Type {
        In,
        Out,
        Unknown
    }
}