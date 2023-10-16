package org.ton.wallet.lib.sqlite.helper

interface SqlTable {

    val tableName: String

    fun getCreateSqlQuery(): String
}