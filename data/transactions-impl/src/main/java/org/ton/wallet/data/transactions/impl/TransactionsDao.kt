package org.ton.wallet.data.transactions.impl

import android.content.ContentValues
import androidx.core.database.*
import org.ton.wallet.core.serializer.ByteArraySerializer
import org.ton.wallet.data.transactions.api.model.*
import org.ton.wallet.lib.sqlite.SqliteDatabase

interface TransactionsDao {

    suspend fun add(accountId: Int, transactions: List<TransactionDto>)

    suspend fun add(accountId: Int, transaction: TransactionDto): Long?

    suspend fun setCancelled(accountId: Int, internalIds: List<Long>)


    suspend fun get(internalId: Long): TransactionDto?

    suspend fun getAll(accountId: Int): List<TransactionDto>

    suspend fun getAllNonExecuted(accountId: Int): List<TransactionDto>

    suspend fun getAllSendUnique(accountId: Int): List<RecentTransactionDto>

    suspend fun getLastExecutedTransactionHashes(accountId: Int): Set<String>

    suspend fun getPendingOutdatedInternalIds(accountId: Int): List<Long>


    suspend fun update(internalId: Long, transaction: TransactionDto)

    suspend fun deleteAll()
}

class TransactionsDaoImpl(
    private val db: SqliteDatabase
) : TransactionsDao {

    override suspend fun add(accountId: Int, transactions: List<TransactionDto>) {
        if (transactions.isEmpty()) {
            return
        }
        val values = ContentValues()
        transactions.forEach { transaction ->
            fillTransactionContentValues(accountId, transaction, values)
            try {
                db.writeableDatabase.insertOrThrow(SqlTableTransactions.tableName, null, values)
            } catch (e: Exception) {
                // L.e(e)
            }
        }
    }

    override suspend fun add(accountId: Int, transaction: TransactionDto): Long? {
        val contentValues = ContentValues()
        fillTransactionContentValues(accountId, transaction, contentValues)
        var internalId: Long? = null
        db.withTransaction {
            internalId = insertOrThrow(SqlTableTransactions.tableName, null, contentValues)
        }
        return internalId
    }

    override suspend fun setCancelled(accountId: Int, internalIds: List<Long>) {
        val contentValues = ContentValues()
        internalIds.forEach { internalId ->
            contentValues.put(SqlTableTransactions.ColumnStatus, TransactionStatus.Cancelled.ordinal)
            contentValues.putNull(SqlTableTransactions.ColumnValidUntil)
            db.writeableDatabase.update(
                table = SqlTableTransactions.tableName,
                values = contentValues,
                whereClause = "${SqlTableTransactions.ColumnInternalId} = ?",
                whereArgs = arrayOf(internalId.toString())
            )
        }
    }

    override suspend fun getAll(accountId: Int): List<TransactionDto> {
        return getTransactions(
            selection = "${SqlTableTransactions.ColumnAccountId} = ?",
            selectionArgs = arrayOf(accountId.toString())
        )
    }

    override suspend fun getAllNonExecuted(accountId: Int): List<TransactionDto> {
        return getTransactions(
            selection = "${SqlTableTransactions.ColumnAccountId} = ? AND ${SqlTableTransactions.ColumnStatus} != ?",
            selectionArgs = arrayOf(accountId.toString(), TransactionStatus.Executed.ordinal.toString())
        )
    }

    override suspend fun getAllSendUnique(accountId: Int): List<RecentTransactionDto> {
        val map = mutableMapOf<String, Long>()

        db.readableDatabase.query(
            table = SqlTableTransactions.tableName,
            columns = arrayOf(SqlTableTransactions.ColumnOutMessages, "MAX(${SqlTableTransactions.ColumnTimestampSec})"),
            selection = "${SqlTableTransactions.ColumnAccountId} = ? AND ${SqlTableTransactions.ColumnOutMessages} IS NOT NULL",
            selectionArgs = arrayOf(accountId.toString()),
            groupBy = SqlTableTransactions.ColumnOutMessages
        )?.use { cursor ->
            val columnOutMessages = cursor.getColumnIndex(SqlTableTransactions.ColumnOutMessages)
            val columnIndexTimestamp = columnOutMessages + 1
            while (cursor.moveToNext()) {
                val outMessagesBytes = cursor.getBlobOrNull(columnOutMessages)
                val timestamp = cursor.getLongOrNull(columnIndexTimestamp)
                if (outMessagesBytes != null && timestamp != null) {
                    val outMessages = ByteArraySerializer.deserialize<List<TransactionMessageDto>>(outMessagesBytes)
                    for (i in outMessages.indices) {
                        val currentAddress = outMessages[i].address ?: continue
                        val currentAddressTimestamp = map.getOrPut(currentAddress) { timestamp }
                        if (currentAddressTimestamp < timestamp) {
                            map[currentAddress] = timestamp
                        }
                    }
                }
            }
        }

        return map.map { (address, timestamp) ->
            RecentTransactionDto(address = address, timestampSec = timestamp)
        }
    }

    override suspend fun get(internalId: Long): TransactionDto? {
        return getTransactions(
            selection = "${SqlTableTransactions.ColumnInternalId} = ?",
            selectionArgs = arrayOf(internalId.toString())
        ).firstOrNull()
    }

    override suspend fun getLastExecutedTransactionHashes(accountId: Int): Set<String> {
        val hashes = hashSetOf<String>()
        val sqlQuery = """
            SELECT ${SqlTableTransactions.ColumnHash}, MAX(${SqlTableTransactions.ColumnTimestampSec})
            FROM ${SqlTableTransactions.tableName}
            WHERE ${SqlTableTransactions.ColumnTimestampSec} = (
                SELECT MAX(${SqlTableTransactions.ColumnTimestampSec})
                FROM ${SqlTableTransactions.tableName}
                WHERE ${SqlTableTransactions.ColumnAccountId} = $accountId AND ${SqlTableTransactions.ColumnStatus} = ${TransactionStatus.Executed.ordinal}
            )
            GROUP BY ${SqlTableTransactions.ColumnHash}
        """.trimIndent()
        db.readableDatabase.rawQuery(sqlQuery, null)?.use { cursor ->
            val columnIndexHash = cursor.getColumnIndex(SqlTableTransactions.ColumnHash)
            while (cursor.moveToNext()) {
                val hash = cursor.getStringOrNull(columnIndexHash)
                if (hash != null) {
                    hashes.add(hash)
                }
            }
        }
        return hashes
    }

    override suspend fun update(internalId: Long, transaction: TransactionDto) {
        var accountId: Int? = null
        db.readableDatabase.query(
            table = SqlTableTransactions.tableName,
            columns = arrayOf(SqlTableTransactions.ColumnAccountId),
            selection = "${SqlTableTransactions.ColumnInternalId} = ?",
            selectionArgs = arrayOf(internalId.toString())
        )?.use { cursor ->
            if (cursor.moveToNext()) {
                val columnIndexAccountId = cursor.getColumnIndex(SqlTableTransactions.ColumnAccountId)
                accountId = cursor.getIntOrNull(columnIndexAccountId)
            }
        }

        if (accountId != null) {
            val contentValues = ContentValues()
            fillTransactionContentValues(accountId!!, transaction, contentValues)
            db.writeableDatabase.update(
                table = SqlTableTransactions.tableName,
                values = contentValues,
                whereClause = "${SqlTableTransactions.ColumnInternalId} = ?",
                whereArgs = arrayOf(internalId.toString())
            )
        }
    }

    override suspend fun getPendingOutdatedInternalIds(accountId: Int): List<Long> {
        val timestampSec = System.currentTimeMillis() / 1000
        return getTransactionsInternalIds(
            selection = "${SqlTableTransactions.ColumnAccountId} = ? AND ${SqlTableTransactions.ColumnValidUntil} < ?",
            selectionArgs = arrayOf(accountId.toString(), timestampSec.toString())
        )
    }

    override suspend fun deleteAll() {
        db.writeableDatabase.delete(table = SqlTableTransactions.tableName, whereClause = null, whereArgs = null)
    }

    private fun fillTransactionContentValues(accountId: Int, transaction: TransactionDto, values: ContentValues) {
        values.clear()
        values.put(SqlTableTransactions.ColumnHash, transaction.hash)
        values.put(SqlTableTransactions.ColumnAccountId, accountId)
        values.put(SqlTableTransactions.ColumnTimestampSec, transaction.timestampSec)
        values.put(SqlTableTransactions.ColumnStatus, transaction.status.ordinal)
        values.put(SqlTableTransactions.ColumnFee, transaction.fee)
        values.put(SqlTableTransactions.ColumnStorageFee, transaction.storageFee)
        transaction.validUntilSec?.let { validUntilSec ->
            values.put(SqlTableTransactions.ColumnValidUntil, validUntilSec)
        }
        transaction.inMessage?.let { inMsg ->
            values.put(SqlTableTransactions.ColumnInMessage, ByteArraySerializer.serialize(inMsg))
        }
        transaction.outMessages?.let { outMessages ->
            if (outMessages.isNotEmpty()) {
                values.put(SqlTableTransactions.ColumnOutMessages, ByteArraySerializer.serialize(outMessages))
            }
        }
    }

    private fun getTransactions(selection: String?, selectionArgs: Array<String?>?): List<TransactionDto> {
        val transactions = mutableListOf<TransactionDto>()
        db.readableDatabase.query(
            table = SqlTableTransactions.tableName,
            selection = selection,
            selectionArgs = selectionArgs,
            orderBy = "${SqlTableTransactions.ColumnTimestampSec} DESC"
        )?.use { cursor ->
            val cursorIndexInternalId = cursor.getColumnIndex(SqlTableTransactions.ColumnInternalId)
            val cursorIndexHash = cursor.getColumnIndex(SqlTableTransactions.ColumnHash)
            val cursorIndexAccountId = cursor.getColumnIndex(SqlTableTransactions.ColumnAccountId)
            val cursorIndexTimestampSec = cursor.getColumnIndex(SqlTableTransactions.ColumnTimestampSec)
            val cursorIndexStatus = cursor.getColumnIndex(SqlTableTransactions.ColumnStatus)
            val cursorIndexFee = cursor.getColumnIndex(SqlTableTransactions.ColumnFee)
            val cursorIndexStorageFee = cursor.getColumnIndex(SqlTableTransactions.ColumnStorageFee)
            val cursorIndexValidUntil = cursor.getColumnIndex(SqlTableTransactions.ColumnValidUntil)
            val cursorIndexInMessage = cursor.getColumnIndex(SqlTableTransactions.ColumnInMessage)
            val cursorIndexOutMessages = cursor.getColumnIndex(SqlTableTransactions.ColumnOutMessages)
            while (cursor.moveToNext()) {
                val inMsgByteArray = cursor.getBlobOrNull(cursorIndexInMessage)
                val outMsgByteArray = cursor.getBlobOrNull(cursorIndexOutMessages)
                val inMessage = inMsgByteArray?.let { ByteArraySerializer.deserialize<TransactionMessageDto>(it) }
                val outMessages = outMsgByteArray?.let { ByteArraySerializer.deserialize<List<TransactionMessageDto>>(it) }
                val transaction = TransactionDto(
                    internalId = cursor.getLong(cursorIndexInternalId),
                    hash = cursor.getString(cursorIndexHash),
                    accountId = cursor.getInt(cursorIndexAccountId),
                    timestampSec = cursor.getLongOrNull(cursorIndexTimestampSec),
                    status = TransactionStatus.entries[cursor.getInt(cursorIndexStatus)],
                    fee = cursor.getLongOrNull(cursorIndexFee),
                    storageFee = cursor.getLongOrNull(cursorIndexStorageFee),
                    inMessage = inMessage,
                    outMessages = outMessages ?: emptyList(),
                    validUntilSec = cursor.getLongOrNull(cursorIndexValidUntil)
                )
                transactions.add(transaction)
            }
        }
        return transactions
    }

    private fun getTransactionsInternalIds(selection: String?, selectionArgs: Array<String?>?): List<Long> {
        val result = mutableListOf<Long>()
        db.readableDatabase.query(
            table = SqlTableTransactions.tableName,
            columns = arrayOf(SqlTableTransactions.ColumnInternalId),
            selection = selection,
            selectionArgs = selectionArgs
        )?.use { cursor ->
            val cursorIndexInternalId = cursor.getColumnIndex(SqlTableTransactions.ColumnInternalId)
            while (cursor.moveToNext()) {
                result.add(cursor.getLong(cursorIndexInternalId))
            }
        }
        return result
    }
}