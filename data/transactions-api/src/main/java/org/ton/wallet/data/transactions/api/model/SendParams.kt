package org.ton.wallet.data.transactions.api.model

import org.ton.wallet.data.core.ton.MessageData
import org.ton.wallet.data.wallet.api.model.AccountDto

class SendParams(
    val account: AccountDto,
    val publicKey: String,
    val messages: List<MessageData>,
    val secret: ByteArray? = null,
    val password: ByteArray? = null,
    val seed: ByteArray? = null,
)