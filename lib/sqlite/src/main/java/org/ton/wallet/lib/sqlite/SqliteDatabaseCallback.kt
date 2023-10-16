package org.ton.wallet.lib.sqlite

interface SqliteDatabaseCallback {

    fun onCreate(db: SQLiteDatabaseWrapper)

    fun onOpen(db: SQLiteDatabaseWrapper)

    fun onUpgrade(db: SQLiteDatabaseWrapper, oldVersion: Int, newVersion: Int) = Unit
}