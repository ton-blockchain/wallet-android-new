package org.ton.wallet.data.transactions.api.model

class TransactionDto(
    var internalId: Long = 0,
    val hash: String,
    val accountId: Int,
    val status: TransactionStatus,
    val timestampSec: Long?,
    val amount: Long?,
    val peerAddress: String?,
    val message: String? = null,
    val fee: Long? = null,
    val storageFee: Long? = null,
    val validUntilSec: Long? = null,
    val inMsgBodyHash: String? = null,
) {

    val type: Type =
        if (amount != null) {
            if (amount > 0) Type.In
            else if (amount < 0) Type.Out
            else Type.Unknown
        } else {
            Type.Unknown
        }

    enum class Type {
        In,
        Out,
        Unknown
    }
}