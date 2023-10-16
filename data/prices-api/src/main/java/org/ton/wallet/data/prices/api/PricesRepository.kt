package org.ton.wallet.data.prices.api

import kotlinx.coroutines.flow.StateFlow
import org.ton.wallet.data.core.BaseRepository
import org.ton.wallet.data.core.model.FiatCurrency

interface PricesRepository : BaseRepository {

    fun getFiatPriceFlow(fiatCurrency: FiatCurrency): StateFlow<Double>

    suspend fun getFiatPrice(fiatCurrency: FiatCurrency): Double

    suspend fun fetchPrices()
}