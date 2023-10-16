package org.ton.wallet.domain.transactions.api.model

import org.ton.wallet.data.transactions.api.model.TransactionDto
import org.ton.wallet.data.transactions.api.model.TransactionStatus

class TransactionDetailsState(
    val hash: String,
    val status: TransactionStatus,
    val type: TransactionDto.Type,
    val amount: Long?,
    val amountString: CharSequence?,
    val fee: String?,
    val date: String?,
    val message: String?,
    val peerAddress: String?,
    val peerDns: String?,
    val peerShortAddress: CharSequence?,
    val hashShort: CharSequence?,
    val buttonText: String
)