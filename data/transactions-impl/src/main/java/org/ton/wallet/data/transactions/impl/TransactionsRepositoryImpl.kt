package org.ton.wallet.data.transactions.impl

import android.util.Base64
import drinkless.org.ton.TonApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.crypto.base64
import org.ton.wallet.core.ext.toIntOrNull
import org.ton.wallet.data.core.BuildConfig
import org.ton.wallet.data.core.model.TonAccount
import org.ton.wallet.data.core.ton.MessageText
import org.ton.wallet.data.core.ton.TonWalletHelper
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
        val lastHashes = transactionsDao.getLastExecutedTransactionHashes(account.id)
        val nonExecutedTransactions = transactionsDao.getAllNonExecuted(account.id)
        val pendingTransactionsHashes = hashSetOf<String>()
        nonExecutedTransactions.forEach { dto ->
            if (dto.status == TransactionStatus.Pending) {
                pendingTransactionsHashes.add(dto.hash)
            }
        }

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

            if (lastHashes.isNotEmpty()) {
                for (rawTransaction in transactionsResponse.transactions) {
                    val rawTransactionHash = Base64.encodeToString(rawTransaction.transactionId.hash, Base64.NO_WRAP)
                    val rawTransactionInMsgHash = Base64.encodeToString(rawTransaction.inMsg?.bodyHash ?: byteArrayOf(), Base64.NO_WRAP)
                    if (pendingTransactionsHashes.contains(rawTransactionHash) || pendingTransactionsHashes.contains(rawTransactionInMsgHash)) {
                        pendingTransactionsHashes.remove(rawTransactionHash)
                        pendingTransactionsHashes.remove(rawTransactionInMsgHash)
                    }
                    apiTransactions.add(rawTransaction)
                }
            } else {
                apiTransactions.addAll(transactionsResponse.transactions)
            }

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
                if (nonExecutedTransaction.hash == dto.hash || nonExecutedTransaction.hash == dto.inMsgBodyHash) {
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
            val (queryInfo, _) = getSendQueryInfo(sendParams)
            val feeRequest = TonApi.QueryEstimateFees(queryInfo.id, true)
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
        if (sendParams.secret == null) throw IllegalArgumentException("Secret is null")
        if (sendParams.password == null) throw IllegalArgumentException("Password is null")
        if (sendParams.seed == null) throw IllegalArgumentException("Seed is null")

        val (queryInfo, message) = getSendQueryInfo(sendParams)
        val sendQuery = TonApi.QuerySend(queryInfo.id)
        tonClient.sendRequestTyped<TonApi.Ok>(sendQuery)

        val bodyHash = queryInfo.bodyHash
        val validUntil = queryInfo.validUntil
        val transaction = TransactionDto(
            hash = Base64.encodeToString(bodyHash, Base64.NO_WRAP),
            accountId = sendParams.account.id,
            status = TransactionStatus.Pending,
            timestampSec = System.currentTimeMillis() / 1000,
            amount = -sendParams.amount,
            peerAddress = sendParams.toAddress,
            message = sendParams.message?.let { msg -> TonWalletHelper.getMessageText(msg, sendParams.seed) },
            validUntilSec = validUntil,
        )
        val internalId = transactionsDao.add(sendParams.account.id, transaction)
        if (internalId != null) {
            transaction.internalId = internalId
            (transactionsAddedFlow as MutableSharedFlow).emit(transaction)
        }

        return SendResult(sendParams.amount, message)
    }

    override suspend fun deleteWallet() {
        transactionsDao.deleteAll()
    }

    private suspend fun getSendQueryInfo(sendParams: SendParams): Pair<TonApi.QueryInfo, Message<Cell>> {
        val getAccountStateRequest = TonApi.RawGetAccountState(TonApi.AccountAddress(sendParams.account.address))
        val accountState = tonClient.sendRequestTyped<TonApi.RawFullAccountState>(getAccountStateRequest)

        val isAccountDeployed = accountState.data.size >= 21
        val seqNo = if(isAccountDeployed) {
            accountState.data.copyOfRange(13, 17).toIntOrNull() ?: 0
        } else 0
        val subWalletId = if(isAccountDeployed) {
            accountState.data.copyOfRange(17, 21).toIntOrNull() ?: TonAccount.DefaultWalletId
        } else TonAccount.DefaultWalletId

        val account = TonAccount(sendParams.publicKey, sendParams.account.version, sendParams.account.revision, subWalletId, seqNo)

        val codeByteArray: ByteArray
        val dataByteArray: ByteArray
        if (sendParams.stateInitBase64 == null) {
            codeByteArray = account.getCode()
            dataByteArray = account.getInitialData()
        } else {
            val stateInit = try {
                StateInit.loadTlb(BagOfCells(base64(sendParams.stateInitBase64!!)).roots.first())
            } catch (e: Exception) {
                throw IllegalArgumentException("StateInit is incorrect")
            }
            codeByteArray = stateInit.code.value?.value?.let { BagOfCells(it).toByteArray() } ?: byteArrayOf()
            dataByteArray = stateInit.data.value?.value?.let { BagOfCells(it).toByteArray() } ?: byteArrayOf()
        }

        val codeCell = BagOfCells(codeByteArray).roots.first()
        val dataCell = BagOfCells(dataByteArray).roots.first()

        val unpackedToAddress = tonClient.sendRequestTyped<TonApi.UnpackedAccountAddress>(TonApi.UnpackAccountAddress(sendParams.toAddress))
        val transferCell = TonWalletHelper.getTransferCell(account, unpackedToAddress.workchainId, unpackedToAddress.addr, sendParams.amount, sendParams.message, sendParams.seed)
        val stateInitCell = if(isAccountDeployed) null else StateInit(code = codeCell, data = dataCell)
        val transferMessage = TonWalletHelper.getTransferMessage(
            address = AddrStd.parseUserFriendly(sendParams.account.address),
            stateInit = stateInitCell,
            transferCell = transferCell,
        )

        val requestBody = BagOfCells(transferCell).toByteArray()
        val request = TonApi.RawCreateQuery(TonApi.AccountAddress(sendParams.account.address), codeByteArray, dataByteArray, requestBody)

        return tonClient.sendRequestTyped<TonApi.QueryInfo>(request) to transferMessage
    }

    private fun mapRawTransactionToDto(raw: TonApi.RawTransaction, accountId: Int): TransactionDto {
        var value = 0L
        var message: String? = null
        var inMsgBodyHash: String? = null
        raw.inMsg?.let { msg ->
            value += msg.value
            message = getMessage(msg.msgData)
            inMsgBodyHash = Base64.encodeToString(msg.bodyHash, Base64.NO_WRAP)
        }
        val outMessages = raw.outMsgs ?: emptyArray<TonApi.RawMessage>()
        outMessages.forEach { msg ->
            value -= msg.value
            if (message.isNullOrEmpty()) {
                message = getMessage(msg.msgData)
            }
        }

        val peerAddress: String? =
            if (value > 0) {
                raw.inMsg?.source?.accountAddress
            } else {
                if (raw.transactionId.lt == 0L) raw.inMsg?.destination?.accountAddress
                else raw.outMsgs?.firstOrNull()?.destination?.accountAddress
            }

        return TransactionDto(
            hash = Base64.encodeToString(raw.transactionId.hash, Base64.NO_WRAP),
            accountId = accountId,
            timestampSec = raw.utime,
            status = TransactionStatus.Executed,
            amount = value,
            fee = raw.fee,
            storageFee = raw.storageFee,
            peerAddress = peerAddress,
            message = message,
            inMsgBodyHash = inMsgBodyHash
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
}