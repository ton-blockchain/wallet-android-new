package org.ton.wallet.data.transactions.api.model

data class TransactionDto(
    var internalId: Long = 0,
    val hash: String,
    val accountId: Int,
    val status: TransactionStatus,
    val timestampSec: Long,
    val lt: Long,
    val inMessage: TransactionMessageDto?,
    val outMessages: List<TransactionMessageDto>?,
    val fee: Long? = null,
    val storageFee: Long? = null,
    val validUntilSec: Long? = null,
    val inMsgBodyHash: String? = null,
) {

    val isMultiMessage: Boolean
        get() = (outMessages?.size ?: 0) > 1

    val type: Type =
        if (outMessages?.isNotEmpty() == true) Type.Out
        else if (inMessage != null) Type.In
        else Type.Unknown

    fun getFirstAddress(): String? {
        return outMessages?.firstOrNull()?.address ?: inMessage?.address
    }

    fun getTextMessage(): String? {
        return outMessages?.firstOrNull()?.message ?: inMessage?.message
    }

    fun getTotalAmount(): Long? {
        return inMessage?.amount ?: outMessages?.sumOf { it.amount ?: 0L }
    }

    enum class Type {
        In,
        Out,
        Unknown
    }
}