package org.ton.wallet.data.prices.impl.dao

import android.content.ContentValues
import org.ton.wallet.lib.sqlite.SqliteDatabase

interface FiatPricesDao {

    suspend fun setPrices(prices: List<Pair<String, Double>>)

    suspend fun getPrices(): List<Pair<String, Double>>

    suspend fun getPrice(currency: String): Double?
}

class FiatPricesDaoImpl(
    private val db: SqliteDatabase
) : FiatPricesDao {

    override suspend fun setPrices(prices: List<Pair<String, Double>>) {
        val values = ContentValues()
        values.put(SqlTableFiatPrices.ColumnTokenId, 0)
        prices.forEach { (currencyCode, price) ->
            values.put(currencyCode, price)
        }
        db.writeableDatabase.update(
            table = SqlTableFiatPrices.tableName,
            values = values,
            whereClause = "${SqlTableFiatPrices.ColumnTokenId} = ?",
            whereArgs = arrayOf("0")
        )
    }

    override suspend fun getPrices(): List<Pair<String, Double>> {
        val prices = mutableListOf<Pair<String, Double>>()
        db.readableDatabase.query(
            table = SqlTableFiatPrices.tableName
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                SqlTableFiatPrices.currenciesColumns.forEach { currency ->
                    val columnId = cursor.getColumnIndex(currency)
                    val price = cursor.getDouble(columnId)
                    prices.add(currency to price)
                }
            }
        }
        return prices
    }

    override suspend fun getPrice(currency: String): Double? {
        return db.readableDatabase.query(
            table = SqlTableFiatPrices.tableName,
            columns = arrayOf(currency),
            selection = "${SqlTableFiatPrices.ColumnTokenId} = ?",
            selectionArgs = arrayOf("0")
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnId = cursor.getColumnIndex(currency)
                cursor.getDouble(columnId)
            } else {
                null
            }
        }
    }
}