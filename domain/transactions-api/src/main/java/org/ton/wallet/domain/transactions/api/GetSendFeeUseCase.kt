package org.ton.wallet.domain.transactions.api

import org.ton.wallet.data.core.model.MessageData

interface GetSendFeeUseCase {

    suspend fun invoke(messages: List<MessageData>): Long
}