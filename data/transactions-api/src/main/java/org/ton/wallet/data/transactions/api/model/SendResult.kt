package org.ton.wallet.data.transactions.api.model

import org.ton.block.Message
import org.ton.cell.Cell
import org.ton.wallet.data.core.ton.MessageData

class SendResult(
    val amount: Long,
    val externalMessage: Message<Cell>,
    // TODO: use out messages to show them in the UI (maybe)
    val outMessages: List<MessageData>
)