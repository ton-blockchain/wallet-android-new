package org.ton.wallet.app.data

import android.content.Context
import org.ton.wallet.app.BuildConfig
import org.ton.wallet.data.prices.impl.dao.SqlTableFiatPrices
import org.ton.wallet.data.tonconnect.impl.SqlTableTonConnect
import org.ton.wallet.data.transactions.impl.SqlTableTransactions
import org.ton.wallet.data.wallet.api.SqlTableAccounts
import org.ton.wallet.lib.sqlite.SQLiteDatabaseWrapper
import org.ton.wallet.lib.sqlite.SqliteDatabaseImpl

internal class AppDataBase(context: Context) : SqliteDatabaseImpl(context, "db", 1, CustomSqlite) {

    override fun onCreate(db: SQLiteDatabaseWrapper) {
        db.executeSql("PRAGMA foreign_keys = ON;")
        db.executeSql(SqlTableAccounts.getCreateSqlQuery())
        db.executeSql(SqlTableFiatPrices.getCreateSqlQuery())
        db.executeSql(SqlTableFiatPrices.getInitSqlQuery())
        db.executeSql(SqlTableTransactions.getCreateSqlQuery())
        db.executeSql(SqlTableTonConnect.getCreateSqlQuery())
    }

    override fun onOpen(db: SQLiteDatabaseWrapper) {
        db.executeSql("PRAGMA foreign_keys = ON;")
    }

    private companion object {

        private val CustomSqlite = !BuildConfig.DEBUG

        init {
            if (CustomSqlite) {
                System.loadLibrary("sqliteX")
            }
        }
    }
}