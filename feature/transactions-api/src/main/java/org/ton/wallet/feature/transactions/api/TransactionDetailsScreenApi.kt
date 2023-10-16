package org.ton.wallet.feature.transactions.api

import android.app.Activity

interface TransactionDetailsScreenApi {

    fun navigateBack()

    fun navigateToSendAmount(address: String, amount: Long?)

    fun navigateToBrowser(activity: Activity, url: String)

    fun preloadUrl(url: String)
}