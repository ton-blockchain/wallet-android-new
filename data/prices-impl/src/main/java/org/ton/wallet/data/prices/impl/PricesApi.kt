package org.ton.wallet.data.prices.impl

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.ton.wallet.data.core.await
import org.ton.wallet.lib.log.L

interface PricesApi {

    suspend fun getPrices(fiatCurrencies: List<String>): List<Pair<String, Double>>
}

class PricesApiImpl(
    private val okHttpClient: OkHttpClient
) : PricesApi {

    override suspend fun getPrices(fiatCurrencies: List<String>): List<Pair<String, Double>> {
        val url = "https://api.coingecko.com/api/v3/simple/price".toHttpUrl().newBuilder()
            .addQueryParameter("ids", "the-open-network")
            .addQueryParameter("vs_currencies", fiatCurrencies.joinToString(","))
            .addQueryParameter("precision", "6")
            .build()
        val request = Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).await()
        val jsonResponse = response.body?.string() ?: ""

        val prices = mutableListOf<Pair<String, Double>>()
        try {
            val jsonObject = JSONObject(jsonResponse)
            val pricesObject = jsonObject.getJSONObject("the-open-network")
            pricesObject.keys().forEach { key ->
                prices.add(key to pricesObject.getDouble(key))
            }
        } catch (e: Exception) {
            L.e(e)
        }

        return prices
    }
}