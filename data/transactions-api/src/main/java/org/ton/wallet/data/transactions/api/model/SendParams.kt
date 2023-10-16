package org.ton.wallet.data.transactions.api.model

class SendParams(
    val fromAddress: String,
    val toAddress: String,
    val amount: Long,
    val message: String?
)