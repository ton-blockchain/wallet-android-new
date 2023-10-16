package org.ton.wallet.domain.wallet.impl

import org.ton.wallet.data.settings.api.SettingsRepository
import org.ton.wallet.data.wallet.api.AccountsRepository
import org.ton.wallet.domain.wallet.api.RefreshCurrentAccountStateUseCase

class RefreshCurrentAccountStateUseCaseImpl(
    private val accountsRepository: AccountsRepository,
    private val settingsRepository: SettingsRepository
) : RefreshCurrentAccountStateUseCase {

    override suspend fun invoke() {
        val accountType = settingsRepository.accountTypeFlow.value ?: return
        val address = accountsRepository.getAccountAddress(accountType)
        accountsRepository.fetchAccountState(address, accountType)
    }
}