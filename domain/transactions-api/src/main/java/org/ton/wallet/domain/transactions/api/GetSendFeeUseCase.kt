package org.ton.wallet.domain.transactions.api

import org.ton.wallet.data.core.ton.MessageData

interface GetSendFeeUseCase {

    suspend fun invoke(toAddress: String, amount: Long, message: MessageData?, stateInitBase64: String? = null): Long
}