package org.ton.wallet.data.transactions.impl

import android.util.Base64
import drinkless.org.ton.TonApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.ton.block.AddrStd
import org.ton.boc.BagOfCells
import org.ton.contract.wallet.WalletV4R2Contract
import org.ton.lite.api.liteserver.LiteServerAccountId
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
import java.nio.charset.Charset

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

    override suspend fun getSendFee(account: AccountDto, publicKey: String, sendParams: SendParams): Long {
        val inputKey = TonApi.InputKeyFake()
        return try {
            val queryInfo = getSendQueryInfo(account, inputKey, null, publicKey, sendParams)
            val feeRequest = TonApi.QueryEstimateFees(queryInfo.id, true)
            val feeResponse = tonClient.sendRequestTyped<TonApi.QueryFees>(feeRequest)
            feeResponse.sourceFees.gasFee + feeResponse.sourceFees.storageFee + feeResponse.sourceFees.fwdFee + feeResponse.sourceFees.inFwdFee
        } catch (e: Exception) {
            if (account.version == 4) {
                0L
            } else {
                throw e
            }
        }
    }

    override suspend fun performSend(account: AccountDto, publicKey: String, secret: ByteArray, password: ByteArray, seed: ByteArray, sendParams: SendParams): Long {
        val inputKey = TonApi.InputKeyRegular(TonApi.Key(publicKey, secret), password)
        val queryInfo = getSendQueryInfo(account, inputKey, seed, publicKey, sendParams)
        val sendQuery = TonApi.QuerySend(queryInfo.id)
        tonClient.sendRequestTyped<TonApi.Ok>(sendQuery)
        val bodyHash = queryInfo.bodyHash
        val validUntil = queryInfo.validUntil

        val transaction = TransactionDto(
            hash = Base64.encodeToString(bodyHash, Base64.NO_WRAP),
            accountId = account.id,
            status = TransactionStatus.Pending,
            timestampSec = System.currentTimeMillis() / 1000,
            amount = -sendParams.amount,
            peerAddress = sendParams.toAddress,
            message = sendParams.message,
            validUntilSec = validUntil,
        )
        val internalId = transactionsDao.add(account.id, transaction)
        if (internalId != null) {
            transaction.internalId = internalId
            (transactionsAddedFlow as MutableSharedFlow).emit(transaction)
        }

        return sendParams.amount
    }

    override suspend fun deleteWallet() {
        transactionsDao.deleteAll()
    }

    private suspend fun getSendQueryInfo(accountDto: AccountDto, inputKey: TonApi.InputKey, seed: ByteArray?, publicKey: String, sendParams: SendParams): TonApi.QueryInfo {
        val request: TonApi.Function
        if (accountDto.version == 4) {
            val liteClient = tonClient.getLiteClient() ?: throw Exception("Lite client is not initialized")

            val unpackedFromAddress = tonClient.sendRequestTyped<TonApi.UnpackedAccountAddress>(TonApi.UnpackAccountAddress(sendParams.fromAddress))
            val fromAddrStd = AddrStd(unpackedFromAddress.workchainId, unpackedFromAddress.addr)
            val blockId = liteClient.getLastBlockId()
            val accountInfo = liteClient.getAccount(LiteServerAccountId(fromAddrStd.workchainId, fromAddrStd.address), blockId)!!
            val walletContract = WalletV4R2Contract(accountInfo)

            val account = TonAccount(publicKey, accountDto.version, accountDto.revision, walletContract.getSubWalletId(), walletContract.getSeqno())
            val unpackedToAddress = tonClient.sendRequestTyped<TonApi.UnpackedAccountAddress>(TonApi.UnpackAccountAddress(sendParams.toAddress))
            val requestBody = TonWalletHelper.getTransferMessageBody(account, unpackedToAddress.workchainId, unpackedToAddress.addr, sendParams.amount, sendParams.message, seed)
            val code = TonWalletHelper.getContractCode(account.type)
            val data = TonWalletHelper.getAccountData(account)
            request = TonApi.RawCreateQuery(TonApi.AccountAddress(sendParams.fromAddress), code, data, requestBody)
        } else {
            val account = TonAccount(publicKey, accountDto.version, accountDto.revision)
            val accountCode = TonWalletHelper.getContractCode(account.type)
            val accountData = TonWalletHelper.getAccountData(account)
            val initialAccountState = TonApi.RawInitialAccountState(accountCode, accountData)
            val messageData =
                if (sendParams.message.isNullOrEmpty()) TonApi.MsgDataText(null)
                else TonApi.MsgDataText(sendParams.message!!.toByteArray(Charset.defaultCharset()))
            val actionMessage = TonApi.ActionMsg(arrayOf(TonApi.MsgMessage(TonApi.AccountAddress(sendParams.toAddress), publicKey, sendParams.amount, messageData, 3)), true)
            request = TonApi.CreateQuery(inputKey, TonApi.AccountAddress(sendParams.fromAddress), 0, actionMessage, initialAccountState)
        }
        return tonClient.sendRequestTyped(request)
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