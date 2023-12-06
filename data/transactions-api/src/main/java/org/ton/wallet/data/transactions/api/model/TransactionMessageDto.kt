package org.ton.wallet.data.transactions.api.model

import kotlinx.serialization.Serializable

@Serializable
class TransactionMessageDto(
    val amount: Long?,
    val address: String?,
    val message: String?,
    val bodyHash: String? = null
)