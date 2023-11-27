package org.ton.wallet.domain.transactions.api.model

import org.ton.wallet.data.transactions.api.model.TransactionDto

abstract class TransactionBaseUiListItem(var isVisible: Boolean = true)

class TransactionHeaderUiListItem(val title: String) : TransactionBaseUiListItem()

class TransactionDataUiListItem(
    val internalId: Long,
    val type: TransactionDto.Type,
    val value: CharSequence?,
    val peerAddressShort: CharSequence?,
    val timeString: String?,
    val feeString: String?,
    val messageText: String?
) : TransactionBaseUiListItem() {

    companion object {

        val Empty = TransactionDataUiListItem(
            internalId = 0,
            type = TransactionDto.Type.Unknown,
            value = null,
            peerAddressShort = null,
            timeString = null,
            feeString = null,
            messageText = null
        )
    }
}