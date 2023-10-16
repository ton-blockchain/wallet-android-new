package org.ton.wallet.data.wallet.api

import org.ton.wallet.data.core.model.TonAccountType
import org.ton.wallet.data.wallet.api.model.AccountDto

interface AccountsDao {

    suspend fun getCount(): Int

    suspend fun put(walletId: Int, address: String, type: TonAccountType): AccountDto?

    suspend fun put(dto: AccountDto)

    suspend fun setLastTransaction(address: String, id: Long, hash: ByteArray)


    suspend fun getAddress(walletId: Int, type: TonAccountType): String?

    suspend fun getAddress(accountId: Int): String?

    suspend fun get(address: String): AccountDto?

    suspend fun get(id: Int): AccountDto?

    suspend fun get(type: TonAccountType): AccountDto?

    suspend fun getAll(): List<AccountDto>

    suspend fun getId(type: TonAccountType): Int?


    suspend fun removeWallet(walletId: Int)
}