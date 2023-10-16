package org.ton.wallet.data.transactions.impl

import android.provider.BaseColumns
import org.ton.wallet.data.wallet.api.SqlTableAccounts
import org.ton.wallet.lib.sqlite.helper.*

object SqlTableTransactions : SqlTable {

    override val tableName = "transactions"

    const val ColumnInternalId = BaseColumns._ID
    const val ColumnHash = "hash"
    const val ColumnAccountId = "accountId"
    const val ColumnTimestampSec = "timestampSec"
    const val ColumnAmount = "amount"
    const val ColumnFee = "fee"
    const val ColumnStorageFee = "storageFee"
    const val ColumnPeerAddress = "peerAddress"
    const val ColumnMessage = "message"
    const val ColumnStatus = "status"
    const val ColumnValidUntil = "validUntil"

    override fun getCreateSqlQuery(): String {
        return SqlTableBuilder(tableName)
            .addColumn(ColumnInternalId, SqlColumnBuilder.Type.INTEGER) {
                isAutoIncrement = true
                isNotNull = true
                isPrimaryKey = true
            }
            .addColumn(ColumnAccountId, SqlColumnBuilder.Type.INTEGER) {
                isNotNull = true
                addReference(SqlTableAccounts.tableName, SqlTableAccounts.ColumnId) {
                    onDelete = SqlColumnReference.Action.Cascade
                    onUpdate = SqlColumnReference.Action.NoAction
                }
            }
            .addColumn(ColumnHash, SqlColumnBuilder.Type.TEXT) {
                isNotNull = true
                isUnique = true
            }
            .addColumn(ColumnStatus, SqlColumnBuilder.Type.INTEGER) { isNotNull = true }
            .addColumn(ColumnTimestampSec, SqlColumnBuilder.Type.INTEGER)
            .addColumn(ColumnAmount, SqlColumnBuilder.Type.INTEGER)
            .addColumn(ColumnFee, SqlColumnBuilder.Type.INTEGER)
            .addColumn(ColumnStorageFee, SqlColumnBuilder.Type.INTEGER)
            .addColumn(ColumnPeerAddress, SqlColumnBuilder.Type.TEXT)
            .addColumn(ColumnMessage, SqlColumnBuilder.Type.TEXT)
            .addColumn(ColumnValidUntil, SqlColumnBuilder.Type.INTEGER)
            .buildCreateSql()
    }
}