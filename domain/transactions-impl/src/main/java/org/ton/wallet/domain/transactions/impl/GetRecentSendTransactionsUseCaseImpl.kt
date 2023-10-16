package org.ton.wallet.domain.transactions.impl

import org.ton.wallet.data.settings.api.SettingsRepository
import org.ton.wallet.data.transactions.api.TransactionsRepository
import org.ton.wallet.data.transactions.api.model.RecentTransactionDto
import org.ton.wallet.data.wallet.api.AccountsRepository
import org.ton.wallet.domain.transactions.api.GetRecentSendTransactionsUseCase

class GetRecentSendTransactionsUseCaseImpl(
    private val accountsRepository: AccountsRepository,
    private val settingsRepository: SettingsRepository,
    private val transactionsRepository: TransactionsRepository
) : GetRecentSendTransactionsUseCase {

    override suspend fun invoke(): List<RecentTransactionDto> {
        val type = settingsRepository.accountTypeFlow.value ?: return emptyList()
        val accountId = accountsRepository.getAccountId(type) ?: return emptyList()
        return transactionsRepository.getLocalRecentSendTransactions(accountId)
    }
}