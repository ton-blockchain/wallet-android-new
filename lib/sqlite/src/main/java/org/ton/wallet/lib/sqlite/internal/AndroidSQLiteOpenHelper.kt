package org.ton.wallet.lib.sqlite.internal

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.ton.wallet.lib.sqlite.SQLiteDatabaseWrapper
import org.ton.wallet.lib.sqlite.SqliteDatabaseCallback
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal open class AndroidSQLiteOpenHelper(
    private val callback: SqliteDatabaseCallback,
    context: Context,
    name: String,
    version: Int,
    factory: SQLiteDatabase.CursorFactory? = null,
    errorHandler: DatabaseErrorHandler? = null
) : SQLiteOpenHelper(context, name, factory, version, errorHandler),
    BaseSQLiteOpenHelper {

    private var _executor: Executor? = null
    override val executor: Executor
        get() {
            if (_executor == null) {
                _executor = Executors.newFixedThreadPool(4)
            }
            return _executor!!
        }

    override val readDatabase: SQLiteDatabaseWrapper = AndroidSQLiteDataBaseWrapper(readableDatabase)

    override val writeDatabase: SQLiteDatabaseWrapper = AndroidSQLiteDataBaseWrapper(writableDatabase)

    override fun onCreate(db: SQLiteDatabase) {
        callback.onCreate(AndroidSQLiteDataBaseWrapper(db))
    }

    override fun onOpen(db: SQLiteDatabase) {
        callback.onOpen(AndroidSQLiteDataBaseWrapper(db))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        callback.onUpgrade(AndroidSQLiteDataBaseWrapper(db), oldVersion, newVersion)
    }

    override fun setExecutor(executor: Executor) {
        this._executor = executor
    }

    private class AndroidSQLiteDataBaseWrapper(
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
            super.executeSql(query)
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

        override fun rawQuery(sql: String, selectionArgs: Array<String?>?): Cursor? {
            return database.rawQuery(sql, selectionArgs)
        }

        override fun update(table: String?, values: ContentValues?, whereClause: String?, whereArgs: Array<String?>?): Int {
            return database.update(table, values, whereClause, whereArgs)
        }
    }
}

