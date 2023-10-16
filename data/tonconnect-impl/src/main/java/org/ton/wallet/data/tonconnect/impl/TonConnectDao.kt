package org.ton.wallet.data.tonconnect.impl

import android.content.ContentValues
import org.ton.wallet.data.tonconnect.api.model.TonConnectDto
import org.ton.wallet.lib.sqlite.SqliteDatabase

interface TonConnectDao {

    suspend fun addConnection(dto: TonConnectDto)

    suspend fun getConnections(accountId: Int): List<TonConnectDto>

    suspend fun getConnection(accountId: Int, clientId: String): TonConnectDto?

    suspend fun hasConnection(accountId: Int, clientId: String): Boolean

    suspend fun updateRequestId(accountId: Int, clientId: String, requestId: Int)

    suspend fun removeConnection(accountId: Int, clientId: String)

    suspend fun removeAllConnections()
}

class TonConnectDaoImpl(
    private val db: SqliteDatabase
) : TonConnectDao {

    override suspend fun addConnection(dto: TonConnectDto) {
        val values = ContentValues()
        values.put(SqlTableTonConnect.ColumnAccountId, dto.accountId)
        values.put(SqlTableTonConnect.ColumnClientId, dto.clientId)
        values.put(SqlTableTonConnect.ColumnPublicKey, dto.publicKey)
        values.put(SqlTableTonConnect.ColumnSecretKey, dto.secretKey)
        db.writeableDatabase.insertOrThrow(
            tableName = SqlTableTonConnect.tableName,
            nullColumnHack = null,
            contentValues = values
        )
    }

    override suspend fun getConnections(accountId: Int): List<TonConnectDto> {
        val connections = mutableListOf<TonConnectDto>()
        db.readableDatabase.query(
            table = SqlTableTonConnect.tableName,
            columns = arrayOf(SqlTableTonConnect.ColumnClientId, SqlTableTonConnect.ColumnPublicKey, SqlTableTonConnect.ColumnSecretKey, SqlTableTonConnect.ColumnRequestId),
            selection = "${SqlTableTonConnect.ColumnAccountId} = ?",
            selectionArgs = arrayOf(accountId.toString())
        )?.use { cursor ->
            val columnIndexClientId = cursor.getColumnIndex(SqlTableTonConnect.ColumnClientId)
            val columnIndexPublicKey = cursor.getColumnIndex(SqlTableTonConnect.ColumnPublicKey)
            val columnIndexSecretKey = cursor.getColumnIndex(SqlTableTonConnect.ColumnSecretKey)
            val columnIndexRequestId = cursor.getColumnIndex(SqlTableTonConnect.ColumnRequestId)
            while (cursor.moveToNext()) {
                val clientId = cursor.getString(columnIndexClientId)
                val publicKey = cursor.getString(columnIndexPublicKey)
                val secretKey = cursor.getString(columnIndexSecretKey)
                val requestId = cursor.getInt(columnIndexRequestId)
                connections.add(TonConnectDto(accountId, clientId, publicKey, secretKey, requestId))
            }
        }
        return connections
    }

    override suspend fun getConnection(accountId: Int, clientId: String): TonConnectDto? {
        db.readableDatabase.query(
            table = SqlTableTonConnect.tableName,
            columns = arrayOf(SqlTableTonConnect.ColumnPublicKey, SqlTableTonConnect.ColumnSecretKey, SqlTableTonConnect.ColumnRequestId),
            selection = "${SqlTableTonConnect.ColumnAccountId} = ? AND ${SqlTableTonConnect.ColumnClientId} = ?",
            selectionArgs = arrayOf(accountId.toString(), clientId)
        )?.use { cursor ->
            val columnIndexPublicKey = cursor.getColumnIndex(SqlTableTonConnect.ColumnPublicKey)
            val columnIndexSecretKey = cursor.getColumnIndex(SqlTableTonConnect.ColumnSecretKey)
            val columnIndexRequestId = cursor.getColumnIndex(SqlTableTonConnect.ColumnRequestId)
            while (cursor.moveToNext()) {
                val publicKey = cursor.getString(columnIndexPublicKey)
                val secretKey = cursor.getString(columnIndexSecretKey)
                val requestId = cursor.getInt(columnIndexRequestId)
                return TonConnectDto(accountId, clientId, publicKey, secretKey, requestId)
            }
        }
        return null
    }

    override suspend fun hasConnection(accountId: Int, clientId: String): Boolean {
        db.readableDatabase.query(
            table = SqlTableTonConnect.tableName,
            columns = arrayOf(SqlTableTonConnect.ColumnAccountId),
            selection = "${SqlTableTonConnect.ColumnAccountId} = ? AND ${SqlTableTonConnect.ColumnClientId} = ?",
            selectionArgs = arrayOf(accountId.toString(), clientId),
            limit = "1"
        )?.use { cursor ->
            return cursor.count > 0
        }
        return false
    }

    override suspend fun updateRequestId(accountId: Int, clientId: String, requestId: Int) {
        val values = ContentValues().apply {
            put(SqlTableTonConnect.ColumnRequestId, requestId)
        }
        db.readableDatabase.update(
            table = SqlTableTonConnect.tableName,
            whereClause = "${SqlTableTonConnect.ColumnAccountId} = ? AND ${SqlTableTonConnect.ColumnClientId} = ?",
            whereArgs = arrayOf(accountId.toString(), clientId),
            values = values
        )
    }

    override suspend fun removeConnection(accountId: Int, clientId: String) {
        db.writeableDatabase.delete(
            table = SqlTableTonConnect.tableName,
            whereClause = "${SqlTableTonConnect.ColumnAccountId} = ? AND ${SqlTableTonConnect.ColumnClientId} = ?",
            whereArgs = arrayOf(accountId.toString(), clientId)
        )
    }

    override suspend fun removeAllConnections() {
        db.writeableDatabase.delete(
            table = SqlTableTonConnect.tableName,
            whereClause = null,
            whereArgs = null
        )
    }
}