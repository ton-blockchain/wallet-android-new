package org.ton.wallet.lib.sqlite

import android.content.Context
import org.ton.wallet.lib.sqlite.internal.*

abstract class SqliteDatabaseImpl(
    private val context: Context,
    private val name: String,
    version: Int,
    isCustom: Boolean = false
) : SqliteDatabaseCallback, SqliteDatabase {

    private val impl: BaseSQLiteOpenHelper =
        if (isCustom) CustomSQLiteOpenHelper(this, context, name, version)
        else AndroidSQLiteOpenHelper(this, context, name, version)

    override val readableDatabase: SQLiteDatabaseWrapper = impl.readDatabase

    override val writeableDatabase: SQLiteDatabaseWrapper = impl.writeDatabase

    override suspend fun withTransaction(block: SQLiteDatabaseWrapper.() -> Unit) {
        impl.withTransaction(block)
    }

    override fun deleteDatabase() {
        context.deleteDatabase(name)
    }
}