package org.ton.wallet.domain.transactions.api

import org.ton.wallet.domain.transactions.api.model.TransactionBaseUiListItem

interface GetTransactionsUseCase {

    suspend fun invoke(isReload: Boolean): List<TransactionBaseUiListItem>?
}