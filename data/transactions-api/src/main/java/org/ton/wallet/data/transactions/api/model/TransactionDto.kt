package org.ton.wallet.data.transactions.api.model

class TransactionDto(
    var internalId: Long = 0,
    val hash: String,
    val accountId: Int,
    val status: TransactionStatus,
    val timestampSec: Long?,
    val inMessage: InMessageDto?,
    val outMessages: List<OutMessageDto>,
    val fee: Long? = null,
    val storageFee: Long? = null,
    val validUntilSec: Long? = null,
    val inMsgBodyHash: String? = null,
) {
    // TODO: remove usage of amount, peerAddress, message, type
    @Deprecated("Remove usage of amount")
    val amount: Long? = inMessage?.amount ?: outMessages.firstOrNull()?.amount
    @Deprecated("Remove usage of peerAddress")
    val peerAddress: String? = inMessage?.source ?: outMessages.firstOrNull()?.destination
    @Deprecated("Remove usage of message")
    val message: String? = null
    @Deprecated("Remove usage of type")
    val type: Type = inMessage?.type ?: outMessages.firstOrNull()?.type ?: Type.Unknown

    enum class Type {
        In,
        Out,
        Unknown
    }
}

class InMessageDto (
    val amount: Long?,
    val source: String?,
    val message: String?,
    val inMsgBodyHash: String?,
) {

    // TODO: in message can be only Type.In
    val type: TransactionDto.Type =
        if (amount != null) {
            if (amount > 0) TransactionDto.Type.In
            else if (amount < 0) TransactionDto.Type.Out
            else TransactionDto.Type.Unknown
        } else {
            TransactionDto.Type.Unknown
        }
}

class OutMessageDto (
    val amount: Long?,
    val destination: String?,
    val message: String?
) {

    // TODO: out message can be only Type.Out
    val type: TransactionDto.Type =
        if (amount != null) {
            if (amount > 0) TransactionDto.Type.In
            else if (amount < 0) TransactionDto.Type.Out
            else TransactionDto.Type.Unknown
        } else {
            TransactionDto.Type.Unknown
        }
}