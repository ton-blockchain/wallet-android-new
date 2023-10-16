package org.ton.wallet.data.wallet.api

import kotlinx.coroutines.flow.StateFlow
import org.ton.wallet.data.core.BaseRepository
import org.ton.wallet.data.core.model.TonAccountType
import org.ton.wallet.data.wallet.api.model.AccountDto

interface AccountsRepository : BaseRepository {

    suspend fun getAccountStateFlow(address: String): StateFlow<AccountDto?>

    suspend fun getAccountsCount(): Int

    suspend fun getAccountAddress(type: TonAccountType): String

    suspend fun getAccountId(type: TonAccountType): Int?

    suspend fun fetchAccountState(address: String, type: TonAccountType): AccountDto

    suspend fun createAccount(type: TonAccountType): AccountDto
}
