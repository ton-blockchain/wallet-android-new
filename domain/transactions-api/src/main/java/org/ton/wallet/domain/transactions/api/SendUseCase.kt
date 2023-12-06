package org.ton.wallet.domain.transactions.api

import org.ton.wallet.data.core.model.MessageData
import org.ton.wallet.data.transactions.api.model.SendResult

interface SendUseCase {

    suspend fun invoke(messages: List<MessageData>): SendResult
}