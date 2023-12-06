package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.feature.send.api.SendAmountScreenApi
import org.ton.wallet.feature.send.impl.confirm.SendConfirmScreenArguments

class SendAmountScreenApiImpl(
    private val navigator: Navigator
) : SendAmountScreenApi {

    override fun navigateBack() {
        navigator.pop(false)
    }

    override fun navigateToConfirm(address: String, amount: Long, isAllAmount: Boolean, message: String?) {
        navigator.push(SendConfirmScreenArguments(
            address = address,
            amount = amount,
            isAllAmount = isAllAmount,
            message = message
        ))
    }
}