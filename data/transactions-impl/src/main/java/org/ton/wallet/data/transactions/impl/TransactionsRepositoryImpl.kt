package org.ton.wallet.data.transactions.impl

import android.util.Base64
import drinkless.org.ton.TonApi
import drinkless.org.ton.TonApi.QueryInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.tlb.loadTlb
import org.ton.wallet.data.core.BuildConfig
import org.ton.wallet.data.core.model.*
import org.ton.wallet.data.core.ton.MessageText
import org.ton.wallet.data.core.util.LoadType
import org.ton.wallet.data.tonclient.api.TonClient
import org.ton.wallet.data.tonclient.api.sendRequestTyped
import org.ton.wallet.data.transactions.api.TransactionsRepository
import org.ton.wallet.data.transactions.api.model.*
import org.ton.wallet.data.wallet.api.model.AccountDto
import org.ton.wallet.lib.log.L

class TransactionsRepositoryImpl(
    private val tonClient: TonClient,
    private val transactionsDao: TransactionsDao
) : TransactionsRepository {

    override val transactionsAddedFlow: Flow<TransactionDto?> = MutableSharedFlow()

    override suspend fun getTransaction(internalId: Long): TransactionDto? {
        return transactionsDao.get(internalId)
    }

    override suspend fun getTransactions(account: AccountDto, loadType: LoadType): List<TransactionDto>? {
        // load transactions from db
        var transactionsDtoList: List<TransactionDto>? = null
        if (loadType.useCache) {
            transactionsDtoList = transactionsDao.getAll(account.id)
        }
        if (loadType == LoadType.OnlyCache || loadType == LoadType.CacheOrApi && !transactionsDtoList.isNullOrEmpty()) {
            return if (transactionsDtoList.isNullOrEmpty()) null else transactionsDtoList
        }

        // check account
        val lastTransactionId = account.lastTransactionId
        val lastTransactionHash = account.lastTransactionHash
        if (lastTransactionId == null || lastTransactionHash == null) {
            return null
        }

        // collect pending transactions
        val nonExecutedTransactions = transactionsDao.getAllNonExecuted(account.id)

        // load transactions from api
        val accountAddress = TonApi.AccountAddress(account.address)
        var fromId: TonApi.InternalTransactionId? = TonApi.InternalTransactionId(lastTransactionId, lastTransactionHash)
        val apiTransactions = mutableListOf<TonApi.RawTransaction>()
        while (fromId != null) {
            val transactionsRequest = TonApi.RawGetTransactions(null, accountAddress, fromId)
            val transactionsResponse = try {
                val response = tonClient.sendRequestTyped<TonApi.RawTransactions>(transactionsRequest)
                response
            } catch (e: Exception) {
                L.e(e)
                null
            } ?: break

            apiTransactions.addAll(transactionsResponse.transactions)
            fromId = if (transactionsResponse.previousTransactionId.lt == 0L) {
                null
            } else {
                transactionsResponse.previousTransactionId
            }
        }

        // prepare new dto list
        val newDtoList = mutableListOf<TransactionDto>()
        apiTransactions.forEach { rawTransaction ->
            val dto = mapRawTransactionToDto(rawTransaction, account.id)

            // check if this transaction was non-executed locally
            var foundNonExecuted = false
            for (nonExecutedTransaction in nonExecutedTransactions) {
                if (nonExecutedTransaction.hash == dto.hash || nonExecutedTransaction.hash == Base64.encodeToString(rawTransaction.inMsg.bodyHash, Base64.NO_WRAP)) {
                    transactionsDao.update(nonExecutedTransaction.internalId, dto)
                    foundNonExecuted = true
                }
            }

            // if this transaction is new then add to new dto list
            if (!foundNonExecuted) {
                newDtoList.add(dto)
            }
        }

        // put new transactions into db
        try {
            transactionsDao.add(account.id, newDtoList)
        } catch (e: Exception) {
            L.e(e)
            if (BuildConfig.DEBUG) {
                throw e
            }
        }

        return transactionsDao.getAll(account.id)
    }

    override suspend fun getLocalRecentSendTransactions(accountId: Int): List<RecentTransactionDto> {
        return transactionsDao.getAllSendUnique(accountId)
    }

    override suspend fun getSendFee(sendParams: SendParams): Long {
        return try {
            val sendInfo = getSendQueryInfo(sendParams)
            val feeRequest = TonApi.QueryEstimateFees(sendInfo.queryInfo.id, true)
            val feeResponse = tonClient.sendRequestTyped<TonApi.QueryFees>(feeRequest)
            feeResponse.sourceFees.gasFee + feeResponse.sourceFees.storageFee + feeResponse.sourceFees.fwdFee + feeResponse.sourceFees.inFwdFee
        } catch (e: Exception) {
            if (sendParams.account.version == 4) {
                0L
            } else {
                throw e
            }
        }
    }

    override suspend fun performSend(sendParams: SendParams): SendResult {
        if (sendParams.seed == null) {
            throw IllegalArgumentException("Seed is null")
        }

        val sendInfo = getSendQueryInfo(sendParams)
        val queryInfo = sendInfo.queryInfo
        val sendQuery = TonApi.QuerySend(queryInfo.id)
        tonClient.sendRequestTyped<TonApi.Ok>(sendQuery)

        val transaction = TransactionDto(
            hash = Base64.encodeToString(queryInfo.bodyHash, Base64.NO_WRAP),
            accountId = sendParams.account.id,
            status = TransactionStatus.Pending,
            timestampSec = System.currentTimeMillis() / 1000,
            lt = 0,
            validUntilSec = queryInfo.validUntil,
            inMessage = null,
            outMessages = sendParams.messages.map { outMessage ->
                TransactionMessageDto(
                    amount = -outMessage.amount,
                    address = outMessage.destination,
                    message = outMessage.getText(sendParams.seed)
                )
            },
        )
        val internalId = transactionsDao.add(sendParams.account.id, transaction)
        if (internalId != null) {
            transaction.internalId = internalId
            (transactionsAddedFlow as MutableSharedFlow).emit(transaction)
        }

        val amount = transaction.outMessages?.sumOf { it.amount ?: 0L } ?: 0L
        return SendResult(amount, sendInfo.externalMessage, sendParams.messages)
    }

    override suspend fun deleteWallet() {
        transactionsDao.deleteAll()
    }

    @Throws(IllegalArgumentException::class)
    private suspend fun getSendQueryInfo(sendParams: SendParams): SendInfo {
        if (sendParams.messages.isEmpty()) {
            throw IllegalArgumentException("Messages is empty")
        }
        if (sendParams.messages.size > 4) {
            throw IllegalArgumentException("Messages count is more than 4")
        }

        val getAccountStateRequest = TonApi.RawGetAccountState(TonApi.AccountAddress(sendParams.account.address))
        val accountState = tonClient.sendRequestTyped<TonApi.RawFullAccountState>(getAccountStateRequest)
        val accountType = TonAccountType.get(sendParams.account.version, sendParams.account.revision)
        val account = TonAccount.fromData(sendParams.publicKey, accountType, accountState.data)

        val transactionCell = TonMessageBuilder.getTransactionCell(account, sendParams.messages, sendParams.seed)
        val transactionCellBytes = BagOfCells(transactionCell).toByteArray()
        val request = TonApi.RawCreateQuery(
            TonApi.AccountAddress(sendParams.account.address),
            if (account.isAccountDeployed) null else account.getCodeBytes(),
            if (account.isAccountDeployed) null else account.getDataBytes(),
            transactionCellBytes
        )
        val queryInfo = tonClient.sendRequestTyped<QueryInfo>(request)

        val stateInitCell = account.getStateInitOrNullIfDeployed()
        val externalMessage = TonMessageBuilder.buildExternalMessage(
            destAddress = AddrStd.parseUserFriendly(sendParams.account.address),
            stateInit = stateInitCell,
            cell = transactionCell,
        )

        return SendInfo(queryInfo = queryInfo, externalMessage = externalMessage)
    }

    private suspend fun mapRawTransactionToDto(raw: TonApi.RawTransaction, accountId: Int): TransactionDto {
        var isInMsgInternal = false
        val firstDataCell = BagOfCells(raw.data).roots.firstOrNull()
        if (firstDataCell != null) {
            val tx = firstDataCell.beginParse()
                .loadTlb(Transaction)
            val inMsgInfo = tx.r1.value.inMsg.value?.value?.info
            isInMsgInternal = inMsgInfo !is ExtInMsgInfo
        }

        val inMessageDto = if (isInMsgInternal) {
            TransactionMessageDto(
                amount = raw.inMsg.value,
                address = raw.inMsg.source?.accountAddress?.let { getUfNonBounceableAddress(it) },
                message = getMessage(raw.inMsg.msgData),
                bodyHash = raw.inMsg?.let { Base64.encodeToString(it.bodyHash, Base64.NO_WRAP) },
            )
        } else {
            null
        }

        val outMessagesDto = raw.outMsgs?.map { msg ->
            TransactionMessageDto(
                amount = -msg.value,
                address = msg.destination?.accountAddress?.let { getUfNonBounceableAddress(it) },
                message = getMessage(msg.msgData),
                bodyHash = msg?.let { Base64.encodeToString(it.bodyHash, Base64.NO_WRAP) },
            )
        }

        return TransactionDto(
            hash = Base64.encodeToString(raw.transactionId.hash, Base64.NO_WRAP),
            accountId = accountId,
            timestampSec = raw.utime,
            lt = raw.transactionId?.lt ?: 0L,
            status = TransactionStatus.Executed,
            fee = raw.fee,
            storageFee = raw.storageFee,
            inMessage = inMessageDto,
            outMessages = outMessagesDto,
        )
    }

    private fun getMessage(msgData: TonApi.MsgData): String? {
        return when (msgData) {
            is TonApi.MsgDataText -> {
                String(msgData.text)
            }
            is TonApi.MsgDataDecryptedText -> {
                String(msgData.text)
            }
            is TonApi.MsgDataRaw -> {
                try {
                    BagOfCells(msgData.body).roots.firstOrNull()?.let { cell ->
                        (MessageText.loadTlb(cell) as? MessageText.Raw)?.text
                    }
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
    }

    private suspend fun getUfNonBounceableAddress(ufAddress: String): String? {
        return try {
            val unpackedAddress = tonClient.sendRequestTyped<TonApi.UnpackedAccountAddress>(TonApi.UnpackAccountAddress(ufAddress))
            unpackedAddress.bounceable = false
            val response = tonClient.sendRequestTyped<TonApi.AccountAddress>(TonApi.PackAccountAddress(unpackedAddress))
            response.accountAddress
        } catch (e: Exception) {
            null
        }
    }

    private class SendInfo(
        val queryInfo: QueryInfo,
        val externalMessage: Message<Cell>,
    )
}