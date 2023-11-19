package org.ton.wallet.data.transactions.api.model

import org.ton.block.Message
import org.ton.cell.Cell

class SendResult(
    val amount: Long,
    val externalMessage: Message<Cell>
)