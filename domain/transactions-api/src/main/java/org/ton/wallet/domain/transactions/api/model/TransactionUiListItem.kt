package org.ton.wallet.domain.transactions.api.model

import org.ton.wallet.data.transactions.api.model.TransactionDto

abstract class TransactionBaseUiListItem(var isVisible: Boolean = true)

class TransactionHeaderUiListItem(val title: String) : TransactionBaseUiListItem()

class TransactionDataUiListItem(
    val internalId: Long,
    val type: TransactionDto.Type,
    val isPending: Boolean,
    val value: CharSequence?,
    val peerAddressShort: CharSequence?,
    val timeString: String?,
    val feeString: String?,
    val messageText: String?,
    val isMultiMessage: Boolean
) : TransactionBaseUiListItem()