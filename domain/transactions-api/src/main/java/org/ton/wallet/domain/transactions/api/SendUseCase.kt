package org.ton.wallet.domain.transactions.api

import org.ton.wallet.data.core.ton.MessageData
import org.ton.wallet.data.transactions.api.model.SendResult

interface SendUseCase {

    suspend fun invoke(messages: List<MessageData>): SendResult
}