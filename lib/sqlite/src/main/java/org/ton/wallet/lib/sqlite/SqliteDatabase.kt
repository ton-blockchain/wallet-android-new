package org.ton.wallet.lib.sqlite

interface SqliteDatabase {

    val readableDatabase: SQLiteDatabaseWrapper

    val writeableDatabase: SQLiteDatabaseWrapper

    suspend fun withTransaction(block: SQLiteDatabaseWrapper.() -> Unit)

    fun deleteDatabase()
}