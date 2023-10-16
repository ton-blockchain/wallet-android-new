package org.ton.wallet.data.wallet.api

import android.provider.BaseColumns
import org.ton.wallet.lib.sqlite.helper.*

object SqlTableAccounts : SqlTable {

    const val ColumnId = BaseColumns._ID
    const val ColumnAddress = "address"
    const val ColumnWalletId = "walletId"
    const val ColumnVersion = "version"
    const val ColumnRevision = "revision"
    const val ColumnBalance = "balance"
    const val ColumnLastTransactionId = "lastTransactionId"
    const val ColumnLastTransactionHash = "lastTransactionHash"

    override val tableName = "accounts"

    override fun getCreateSqlQuery(): String {
        return SqlTableBuilder(tableName)
            .addColumn(ColumnId, SqlColumnBuilder.Type.INTEGER) {
                isAutoIncrement = true
                isPrimaryKey = true
                isNotNull = true
            }
            .addColumn(ColumnWalletId, SqlColumnBuilder.Type.INTEGER) { isNotNull = true }
            .addColumn(ColumnAddress, SqlColumnBuilder.Type.TEXT) {
                isNotNull = true
                isUnique = true
            }
            .addColumn(ColumnVersion, SqlColumnBuilder.Type.INTEGER) { isNotNull = true }
            .addColumn(ColumnRevision, SqlColumnBuilder.Type.INTEGER) { isNotNull = true }
            .addColumn(ColumnBalance, SqlColumnBuilder.Type.INTEGER) { default = "-1" }
            .addColumn(ColumnLastTransactionId, SqlColumnBuilder.Type.INTEGER)
            .addColumn(ColumnLastTransactionHash, SqlColumnBuilder.Type.BLOB)
            .buildCreateSql()
    }
}