package org.ton.wallet.data.tonclient.api

import drinkless.org.ton.TonApi
import org.ton.lite.client.LiteClient

interface TonClient {

    @Throws(Exception::class)
    suspend fun sendRequest(request: TonApi.Function): TonApi.Object

    @Throws(Exception::class)
    suspend fun getLiteClient(): LiteClient?
}

@Throws(Exception::class)
suspend inline fun <reified T : TonApi.Object> TonClient.sendRequestTyped(request: TonApi.Function): T {
    val result = sendRequest(request)
    if (result is T) {
        return result
    } else {
        throw TypeCastException("$result could not be casted to ${T::class}")
    }
}