package org.ton.wallet.app.screen

import android.app.Activity
import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.app.util.AppBrowserUtils
import org.ton.wallet.feature.send.impl.amount.SendAmountScreenArguments
import org.ton.wallet.feature.transactions.api.TransactionDetailsScreenApi

class TransactionDetailsScreenApiImpl(
    private val navigator: Navigator
) : TransactionDetailsScreenApi {

    override fun navigateBack() {
        navigator.pop(false)
    }

    override fun navigateToSendAmount(address: String, amount: Long?) {
        navigator.push(SendAmountScreenArguments(
            ufAddress = address,
            amount = amount,
            isAddressEditable = false
        ))
    }

    override fun navigateToBrowser(activity: Activity, url: String) {
        AppBrowserUtils.open(activity, url)
    }

    override fun preloadUrl(url: String) {
        AppBrowserUtils.mayLaunchUrl(url)
    }
}