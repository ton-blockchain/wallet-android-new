package org.ton.wallet.lib.sqlite.internal

import org.ton.wallet.lib.sqlite.SQLiteDatabaseWrapper
import java.util.concurrent.Executor

internal interface BaseSQLiteOpenHelper {

    val executor: Executor

    val readDatabase: SQLiteDatabaseWrapper

    val writeDatabase: SQLiteDatabaseWrapper

    fun setExecutor(executor: Executor)
}