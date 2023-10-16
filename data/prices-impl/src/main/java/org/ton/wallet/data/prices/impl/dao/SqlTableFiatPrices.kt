package org.ton.wallet.data.prices.impl.dao

import org.ton.wallet.data.core.model.FiatCurrency
import org.ton.wallet.lib.sqlite.helper.*

object SqlTableFiatPrices : SqlTable {

    override val tableName = "fiatPrices"

    const val ColumnTokenId = "tokenId"

    val currenciesColumns = FiatCurrency.entries.map { it.name.lowercase() }

    override fun getCreateSqlQuery(): String {
        return SqlTableBuilder(tableName)
            .addColumn(ColumnTokenId, SqlColumnBuilder.Type.INTEGER) { isNotNull = true }
            .apply {
                currenciesColumns.forEach { currency ->
                    addColumn(currency, SqlColumnBuilder.Type.REAL) {
                        default = "0"
                        isNotNull = true
                    }
                }
            }
            .buildCreateSql()
    }

    fun getInitSqlQuery(): String {
        return "INSERT INTO $tableName ($ColumnTokenId) VALUES (0)"
    }
}