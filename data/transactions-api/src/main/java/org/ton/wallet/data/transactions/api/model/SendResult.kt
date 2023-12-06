package org.ton.wallet.data.transactions.api.model

import org.ton.block.Message
import org.ton.cell.Cell
import org.ton.wallet.data.core.model.MessageData

class SendResult(
    val amount: Long,
    val externalMessage: Message<Cell>,
    val outMessages: List<MessageData>
)