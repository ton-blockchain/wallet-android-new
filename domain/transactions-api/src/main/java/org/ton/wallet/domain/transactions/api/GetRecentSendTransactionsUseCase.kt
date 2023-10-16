package org.ton.wallet.domain.transactions.api

import org.ton.wallet.data.transactions.api.model.RecentTransactionDto

interface GetRecentSendTransactionsUseCase {

    suspend fun invoke(): List<RecentTransactionDto>
}