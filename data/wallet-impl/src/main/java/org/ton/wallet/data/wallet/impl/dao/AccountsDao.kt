package org.ton.wallet.data.wallet.impl.dao

import android.content.ContentValues
import androidx.core.database.*
import org.ton.wallet.data.core.model.TonAccountType
import org.ton.wallet.data.wallet.api.AccountsDao
import org.ton.wallet.data.wallet.api.SqlTableAccounts
import org.ton.wallet.data.wallet.api.model.AccountDto
import org.ton.wallet.lib.log.L
import org.ton.wallet.lib.sqlite.SqliteDatabase

class AccountsDaoImpl(
    private val db: SqliteDatabase
) : AccountsDao {

    override suspend fun getCount(): Int {
        db.readableDatabase.query(table = SqlTableAccounts.tableName).use { cursor ->
            return cursor?.count ?: 0
        }
    }

    override suspend fun put(walletId: Int, address: String, type: TonAccountType): AccountDto? {
        val values = ContentValues()
        values.put(SqlTableAccounts.ColumnWalletId, walletId)
        values.put(SqlTableAccounts.ColumnAddress, address)
        values.put(SqlTableAccounts.ColumnVersion, type.version)
        values.put(SqlTableAccounts.ColumnRevision, type.revision)
        db.writeableDatabase.insert(SqlTableAccounts.tableName, null, values)
        return get(address)
    }

    override suspend fun put(dto: AccountDto) {
        val values = ContentValues()
        values.put(SqlTableAccounts.ColumnWalletId, dto.walletId)
        values.put(SqlTableAccounts.ColumnAddress, dto.address)
        values.put(SqlTableAccounts.ColumnVersion, dto.version)
        values.put(SqlTableAccounts.ColumnRevision, dto.revision)
        values.put(SqlTableAccounts.ColumnBalance, dto.balance)
        values.put(SqlTableAccounts.ColumnLastTransactionId, dto.lastTransactionId)
        values.put(SqlTableAccounts.ColumnLastTransactionHash, dto.lastTransactionHash)
        try {
            val isAccountExists = get(dto.address) != null
            if (isAccountExists) {
                db.writeableDatabase.update(SqlTableAccounts.tableName, values, "${SqlTableAccounts.ColumnAddress} = ?", arrayOf(dto.address))
            } else {
                db.writeableDatabase.insertOrThrow(SqlTableAccounts.tableName, null, values).toInt()
            }
        } catch (e: Exception) {
            L.e(e)
        }
    }

    override suspend fun setLastTransaction(address: String, id: Long, hash: ByteArray) {
        val values = ContentValues().apply {
            put(SqlTableAccounts.ColumnLastTransactionId, id)
            put(SqlTableAccounts.ColumnLastTransactionHash, hash)
        }
        set("${SqlTableAccounts.ColumnAddress} = ?", arrayOf(address), values)
    }


    override suspend fun getAddress(walletId: Int, type: TonAccountType): String? {
        return db.readableDatabase.query(
            table = SqlTableAccounts.tableName,
            columns = arrayOf(SqlTableAccounts.ColumnAddress),
            selection = "${SqlTableAccounts.ColumnWalletId} = ? AND ${SqlTableAccounts.ColumnVersion} = ? AND ${SqlTableAccounts.ColumnRevision} = ?",
            selectionArgs = arrayOf(walletId.toString(), type.version.toString(), type.revision.toString()),
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val addressIndex = cursor.getColumnIndex(SqlTableAccounts.ColumnAddress)
                cursor.getString(addressIndex)
            } else {
                null
            }
        }
    }

    override suspend fun getAddress(id: Int): String? {
        return db.readableDatabase.query(
            table = SqlTableAccounts.tableName,
            columns = arrayOf(SqlTableAccounts.ColumnAddress),
            selection = "${SqlTableAccounts.ColumnId} = ?",
            selectionArgs = arrayOf(id.toString()),
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val addressIndex = cursor.getColumnIndex(SqlTableAccounts.ColumnAddress)
                cursor.getString(addressIndex)
            } else {
                null
            }
        }
    }

    override suspend fun get(address: String): AccountDto? {
        return getAccounts(
            selection = "${SqlTableAccounts.ColumnAddress} = ?",
            selectionArgs = arrayOf(address)
        ).firstOrNull()
    }

    override suspend fun get(id: Int): AccountDto? {
        return getAccounts(
            selection = "${SqlTableAccounts.ColumnId} = ?",
            selectionArgs = arrayOf(id.toString())
        ).firstOrNull()
    }

    override suspend fun get(type: TonAccountType): AccountDto? {
        return getAccounts(
            selection = "${SqlTableAccounts.ColumnVersion} = ? AND ${SqlTableAccounts.ColumnRevision} = ?",
            selectionArgs = arrayOf(type.version.toString(), type.revision.toString())
        ).firstOrNull()
    }

    override suspend fun getAll(): List<AccountDto> {
        return getAccounts(null, null)
    }

    override suspend fun getId(type: TonAccountType): Int? {
        var accountId: Int? = null
        db.readableDatabase.query(
            table = SqlTableAccounts.tableName,
            selection = "${SqlTableAccounts.ColumnVersion} = ? AND ${SqlTableAccounts.ColumnRevision} = ?",
            selectionArgs = arrayOf(type.version.toString(), type.revision.toString()),
        )?.use { cursor ->
            if (cursor.moveToNext()) {
                accountId = cursor.getIntOrNull(cursor.getColumnIndex(SqlTableAccounts.ColumnId))
            }
        }
        return accountId
    }

    override suspend fun removeWallet(walletId: Int) {
        db.writeableDatabase.delete(
            table = SqlTableAccounts.tableName,
            whereClause = "${SqlTableAccounts.ColumnWalletId} = ?",
            whereArgs = arrayOf(walletId.toString())
        )
    }

    private fun set(whereClause: String, whereArgs: Array<String?>?, values: ContentValues) {
        db.writeableDatabase.update(SqlTableAccounts.tableName, values, whereClause, whereArgs)
    }

    private fun getAccounts(selection: String? = null, selectionArgs: Array<String?>? = null): List<AccountDto> {
        val accounts = mutableListOf<AccountDto>()
        db.readableDatabase.query(
            table = SqlTableAccounts.tableName,
            selection = selection,
            selectionArgs = selectionArgs
        )?.use { cursor ->
            val columnIndexId = cursor.getColumnIndex(SqlTableAccounts.ColumnId)
            val columnIndexWalletId = cursor.getColumnIndex(SqlTableAccounts.ColumnWalletId)
            val columnIndex = cursor.getColumnIndex(SqlTableAccounts.ColumnAddress)
            val columnIndexVersion = cursor.getColumnIndex(SqlTableAccounts.ColumnVersion)
            val columnIndexRevision = cursor.getColumnIndex(SqlTableAccounts.ColumnRevision)
            val columnIndexBalance = cursor.getColumnIndex(SqlTableAccounts.ColumnBalance)
            val columnIndexLastTransactionId = cursor.getColumnIndex(SqlTableAccounts.ColumnLastTransactionId)
            val columnIndexLastTransactionHash = cursor.getColumnIndex(SqlTableAccounts.ColumnLastTransactionHash)
            while (cursor.moveToNext()) {
                val account = AccountDto(
                    id = cursor.getInt(columnIndexId),
                    walletId = cursor.getInt(columnIndexWalletId),
                    address = cursor.getString(columnIndex),
                    version = cursor.getInt(columnIndexVersion),
                    revision = cursor.getInt(columnIndexRevision),
                    balance = cursor.getLong(columnIndexBalance),
                    lastTransactionId = cursor.getLongOrNull(columnIndexLastTransactionId),
                    lastTransactionHash = cursor.getBlobOrNull(columnIndexLastTransactionHash)
                )
                accounts.add(account)
            }
        }
        return accounts
    }
}