package org.ton.wallet.domain.wallet.impl

import kotlinx.coroutines.flow.*
import org.ton.wallet.data.settings.api.SettingsRepository
import org.ton.wallet.data.wallet.api.AccountsRepository
import org.ton.wallet.data.wallet.api.model.AccountDto
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase

class GetCurrentAccountDataUseCaseImpl(
    private val accountsRepository: AccountsRepository,
    private val settingsRepository: SettingsRepository,
) : GetCurrentAccountDataUseCase {

    override suspend fun getAccountState(): AccountDto? {
        val accountType = settingsRepository.accountTypeFlow.value ?: return null
        val address = accountsRepository.getAccountAddress(accountType) ?: return null
        return accountsRepository.getAccountStateFlow(address).value
    }

    override fun getAccountStateFlow(): Flow<AccountDto> {
        return getAddressFlow()
            .flatMapLatest(accountsRepository::getAccountStateFlow)
            .filterNotNull()
    }

    override fun getAddressFlow(): Flow<String> {
        return getAddressNullableFlow().filterNotNull()
    }

    override fun getAddressNullableFlow(): Flow<String?> {
        return settingsRepository.accountTypeFlow
            .map { accountType ->
                if (accountType == null) null
                else accountsRepository.getAccountAddress(accountType)
            }
    }

    override fun getIdFlow(): Flow<Int> {
        return settingsRepository.accountTypeFlow
            .filterNotNull()
            .map(accountsRepository::getAccountId)
            .filterNotNull()
            .distinctUntilChanged()
    }
}