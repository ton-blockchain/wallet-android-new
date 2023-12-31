package org.ton.wallet.data.wallet.impl

import android.content.SharedPreferences
import drinkless.org.ton.TonApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.ton.wallet.data.core.SecuredPrefsKeys
import org.ton.wallet.data.core.model.TonAccount
import org.ton.wallet.data.core.model.TonAccountType
import org.ton.wallet.data.core.ton.TonWalletHelper
import org.ton.wallet.data.core.util.CoroutineScopes
import org.ton.wallet.data.tonclient.api.TonClient
import org.ton.wallet.data.tonclient.api.sendRequestTyped
import org.ton.wallet.data.wallet.api.AccountsDao
import org.ton.wallet.data.wallet.api.AccountsRepository
import org.ton.wallet.data.wallet.api.model.AccountDto
import java.util.concurrent.ConcurrentHashMap

class AccountsRepositoryImpl(
    private val tonClient: TonClient,
    private val accountsDao: AccountsDao,
    private val securedPreferences: SharedPreferences
) : AccountsRepository {

    private val accountStateFlows = ConcurrentHashMap<String, MutableStateFlow<AccountDto?>>()

    private val publicKey: String
        get() = securedPreferences.getString(SecuredPrefsKeys.PublicKey, "") ?: ""

    init {
        CoroutineScopes.repositoriesScope.launch(Dispatchers.IO) {
            accountsDao.getAll().forEach { account ->
                getAccountStateMutableFlow(account.address).value = account
            }
        }
    }

    override suspend fun getAccountStateFlow(address: String): StateFlow<AccountDto?> {
        return getAccountStateMutableFlow(address)
    }

    override suspend fun getAccountsCount(): Int {
        return accountStateFlows.size
    }

    override suspend fun getAccountAddress(type: TonAccountType): String {
        return getAccountByType(type).address
    }

    override suspend fun getAccountId(type: TonAccountType): Int {
        return getAccountByType(type).id
    }

    override suspend fun fetchAccountState(address: String, type: TonAccountType): AccountDto {
        val flow = accountStateFlows.getOrPut(address) { MutableStateFlow(null) }
        if (flow.value == null) {
            flow.value = createAccount(type)
        }
        val accountRequest = TonApi.GetAccountState(TonApi.AccountAddress(address))
        val accountResponse = tonClient.sendRequestTyped<TonApi.FullAccountState>(accountRequest)
        val accountDto = AccountDto(
            id = flow.value?.id ?: -1,
            walletId = flow.value?.walletId ?: 0,
            address = flow.value?.address ?: address,
            version = flow.value?.version ?: type.version,
            revision = flow.value?.revision ?: type.revision,
            balance = accountResponse.balance,
            lastTransactionId = accountResponse.lastTransactionId.lt,
            lastTransactionHash = accountResponse.lastTransactionId.hash
        )
        accountsDao.put(accountDto)
        (getAccountStateFlow(address) as MutableStateFlow<AccountDto?>).value = accountDto
        return accountDto
    }

    override suspend fun deleteWallet() {
        accountStateFlows.values.forEach { flow ->
            flow.emit(null)
        }
        accountStateFlows.clear()
        accountsDao.removeWallet(0)
    }

    override suspend fun createAccount(type: TonAccountType): AccountDto {
        var dto = accountsDao.get(type)
        if (dto == null) {
            val account = TonAccount(publicKey, type.version, type.revision)
            val code = TonWalletHelper.getContractCode(type)
            val data = TonWalletHelper.getAccountData(account)
            val initialState = TonApi.RawInitialAccountState(code, data)
            val addressRequest = TonApi.GetAccountAddress(initialState, account.revision, 0)
            val addressResponse = tonClient.sendRequestTyped<TonApi.AccountAddress>(addressRequest)
            val address = addressResponse.accountAddress!!
            dto = accountsDao.put(0, address, type)
            if (dto != null) {
                getAccountStateMutableFlow(address).value = dto
            }
        }
        return dto!!
    }

    private suspend fun getAccountByType(type: TonAccountType): AccountDto {
        var dto = accountStateFlows.values.firstOrNull { flow ->
            val account = flow.value ?: return@firstOrNull false
            account.version == type.version && account.revision == type.revision
        }?.value
        if (dto == null) {
            dto = createAccount(type)
        }
        return dto
    }

    private fun getAccountStateMutableFlow(address: String): MutableStateFlow<AccountDto?> {
        return accountStateFlows.getOrPut(address) { MutableStateFlow(null) }
    }
}