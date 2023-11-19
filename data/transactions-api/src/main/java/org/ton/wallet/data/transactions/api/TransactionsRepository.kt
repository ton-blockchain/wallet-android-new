package org.ton.wallet.data.transactions.api

import kotlinx.coroutines.flow.Flow
import org.ton.wallet.data.core.BaseRepository
import org.ton.wallet.data.core.util.LoadType
import org.ton.wallet.data.transactions.api.model.*
import org.ton.wallet.data.wallet.api.model.AccountDto

interface TransactionsRepository : BaseRepository {

    val transactionsAddedFlow: Flow<TransactionDto?>

    suspend fun getTransaction(internalId: Long): TransactionDto?

    suspend fun getTransactions(account: AccountDto, loadType: LoadType): List<TransactionDto>?

    suspend fun getLocalRecentSendTransactions(accountId: Int): List<RecentTransactionDto>

    @Throws(Exception::class)
    suspend fun getSendFee(sendParams: SendParams): Long

    @Throws(Exception::class)
    suspend fun performSend(sendParams: SendParams): Long
}