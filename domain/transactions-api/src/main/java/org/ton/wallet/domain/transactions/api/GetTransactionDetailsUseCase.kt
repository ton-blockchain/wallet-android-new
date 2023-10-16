package org.ton.wallet.domain.transactions.api

import org.ton.wallet.domain.transactions.api.model.TransactionDetailsState

interface GetTransactionDetailsUseCase {

    suspend fun invoke(id: Long): TransactionDetailsState?
}