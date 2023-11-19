package org.ton.wallet.data.transactions.api.model

import org.ton.wallet.data.core.ton.MessageData
import org.ton.wallet.data.wallet.api.model.AccountDto

class SendParams(
    val account: AccountDto,
    val publicKey: String,
    val toAddress: String,
    val amount: Long,
    val message: MessageData?,
    val stateInitBase64: String?,
    val secret: ByteArray? = null,
    val password: ByteArray? = null,
    val seed: ByteArray? = null,
)