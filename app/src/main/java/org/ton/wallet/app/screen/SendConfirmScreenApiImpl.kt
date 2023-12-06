package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.app.navigation.changehandler.SlideChangeHandler
import org.ton.wallet.feature.passcode.impl.enter.PassCodeEnterScreenArguments
import org.ton.wallet.feature.send.api.SendConfirmScreenApi
import org.ton.wallet.feature.send.impl.processing.SendProcessingScreenArguments

class SendConfirmScreenApiImpl(
    private val navigator: Navigator
) : SendConfirmScreenApi {

    override fun navigateBack() {
        navigator.pop(false)
    }

    override fun navigateToSendProcessing(address: String, amount: Long, isAllAmount: Boolean, fee: Long, message: String?) {
        navigator.push(SendProcessingScreenArguments(address, amount, isAllAmount, fee, message))
    }

    override fun navigateToPassCodeEnter(purpose: String) {
        navigator.push(
            arguments = PassCodeEnterScreenArguments(
                purpose = purpose,
                isDark = false,
                isBackVisible = false
            ),
            pushChangeHandler = SlideChangeHandler.create(false),
            popChangeHandler = SlideChangeHandler.create(false),
        )
    }
}