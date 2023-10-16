package org.ton.wallet.lib.sqlite

import android.content.ContentValues
import android.database.Cursor
import android.util.Log

interface SQLiteDatabaseWrapper {

    fun beginTransaction()

    fun setTransactionSuccessful()

    fun endTransaction()

    fun delete(table: String, whereClause: String?, whereArgs: Array<String>?): Int

    fun executeSql(query: String) {
        Log.d("SQLiteDatabaseWrapper", "execute sql: $query")
    }

    fun insert(tableName: String, nullColumnHack: String?, contentValues: ContentValues): Long

    @Throws(Exception::class)
    fun insertOrThrow(tableName: String, nullColumnHack: String?, contentValues: ContentValues): Long

    fun query(
        table: String,
        columns: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String?>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String? = null,
        limit: String? = null,
        distinct: Boolean = false,
    ): Cursor?

    fun rawQuery(sql: String, selectionArgs: Array<String?>? = null): Cursor?

    fun update(table: String?, values: ContentValues?, whereClause: String?, whereArgs: Array<String?>?): Int
}