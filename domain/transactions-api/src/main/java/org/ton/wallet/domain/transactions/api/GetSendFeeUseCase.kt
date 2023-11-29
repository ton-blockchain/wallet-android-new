package org.ton.wallet.domain.transactions.api

import org.ton.wallet.data.core.ton.MessageData

interface GetSendFeeUseCase {

    suspend fun invoke(messages: List<MessageData>): Long
}