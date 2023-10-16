package org.ton.wallet.lib.sqlite.internal

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import org.sqlite.database.DatabaseErrorHandler
import org.sqlite.database.sqlite.SQLiteDatabase
import org.sqlite.database.sqlite.SQLiteOpenHelper
import org.ton.wallet.lib.sqlite.SQLiteDatabaseWrapper
import org.ton.wallet.lib.sqlite.SqliteDatabaseCallback
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal open class CustomSQLiteOpenHelper(
    private val callback: SqliteDatabaseCallback,
    context: Context,
    name: String,
    version: Int,
    factory: SQLiteDatabase.CursorFactory? = null,
    minimumSupportedVersion: Int = 0,
    errorHandler: DatabaseErrorHandler? = null
) : SQLiteOpenHelper(context, name, factory, version, minimumSupportedVersion, errorHandler),
    BaseSQLiteOpenHelper {

    private var _executor: Executor? = null
    override val executor: Executor
        get() {
            if (_executor == null) {
                _executor = Executors.newFixedThreadPool(4)
            }
            return _executor!!
        }

    override val readDatabase: SQLiteDatabaseWrapper = CustomSQLiteDataBaseWrapper(readableDatabase)

    override val writeDatabase: SQLiteDatabaseWrapper = CustomSQLiteDataBaseWrapper(writableDatabase)

    override fun onCreate(db: SQLiteDatabase) {
        callback.onCreate(CustomSQLiteDataBaseWrapper(db))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        callback.onUpgrade(CustomSQLiteDataBaseWrapper(db), oldVersion, newVersion)
    }

    override fun setExecutor(executor: Executor) {
        this._executor = executor
    }

    private class CustomSQLiteDataBaseWrapper(
        private val database: SQLiteDatabase
    ) : SQLiteDatabaseWrapper {

        override fun beginTransaction() {
            database.beginTransaction()
        }

        override fun endTransaction() {
            database.endTransaction()
        }

        override fun setTransactionSuccessful() {
            database.setTransactionSuccessful()
        }

        override fun delete(table: String, whereClause: String?, whereArgs: Array<String>?): Int {
            return database.delete(table, whereClause, whereArgs)
        }

        override fun executeSql(query: String) {
            database.execSQL(query)
        }

        override fun insert(tableName: String, nullColumnHack: String?, contentValues: ContentValues): Long {
            return database.insert(tableName, nullColumnHack, contentValues)
        }

        override fun insertOrThrow(tableName: String, nullColumnHack: String?, contentValues: ContentValues): Long {
            return database.insertOrThrow(tableName, nullColumnHack, contentValues)
        }

        override fun query(table: String, columns: Array<String>?, selection: String?, selectionArgs: Array<String?>?, groupBy: String?, having: String?, orderBy: String?, limit: String?, distinct: Boolean): Cursor? {
            return database.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        }

        override fun update(table: String?, values: ContentValues?, whereClause: String?, whereArgs: Array<String?>?): Int {
            return database.update(table, values, whereClause, whereArgs)
        }

        override fun rawQuery(sql: String, selectionArgs: Array<String?>?): Cursor? {
            return database.rawQuery(sql, selectionArgs)
        }
    }
}

