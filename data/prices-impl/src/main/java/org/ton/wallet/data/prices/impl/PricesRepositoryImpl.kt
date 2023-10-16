package org.ton.wallet.data.prices.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.ton.wallet.data.core.model.FiatCurrency
import org.ton.wallet.data.core.util.CoroutineScopes
import org.ton.wallet.data.prices.api.PricesRepository
import org.ton.wallet.data.prices.impl.dao.FiatPricesDao
import java.util.concurrent.ConcurrentHashMap

class PricesRepositoryImpl(
    private val pricesApi: PricesApi,
    private val pricesDao: FiatPricesDao
) : PricesRepository {

    private val fiatPricesFlow = ConcurrentHashMap<FiatCurrency, MutableStateFlow<Double>>()

    init {
        CoroutineScopes.repositoriesScope.launch(Dispatchers.IO) {
            val daoPrices = pricesDao.getPrices()
            daoPrices.forEach { (currency, price) ->
                val fiatCurrency = FiatCurrency.valueOf(currency.uppercase())
                fiatPricesFlow.getOrPut(fiatCurrency) { MutableStateFlow(0.0) }.tryEmit(price)
            }
        }
    }

    override fun getFiatPriceFlow(fiatCurrency: FiatCurrency): MutableStateFlow<Double> {
        var flow = fiatPricesFlow[fiatCurrency]
        if (flow == null) {
            flow = MutableStateFlow(0.0)
            fiatPricesFlow[fiatCurrency] = flow
            CoroutineScopes.repositoriesScope.launch(Dispatchers.IO) {
                flow.value = getFiatPrice(fiatCurrency)
            }
        }
        return flow
    }

    override suspend fun getFiatPrice(fiatCurrency: FiatCurrency): Double {
        var value = pricesDao.getPrice(fiatCurrency.name.lowercase())
        if (value == null) {
            fetchPrices()
            value = pricesDao.getPrice(fiatCurrency.name.lowercase())
        }
        return value ?: 0.0
    }

    override suspend fun fetchPrices() {
        val fiatCurrencies = FiatCurrency.values().map { it.name.lowercase() }
        val prices = pricesApi.getPrices(fiatCurrencies)
        pricesDao.setPrices(prices)
        prices.forEach { (currency, price) ->
            val fiatCurrency = FiatCurrency.valueOf(currency.uppercase())
            fiatPricesFlow[fiatCurrency]?.tryEmit(price)
        }
    }

    override suspend fun deleteWallet() = Unit
}