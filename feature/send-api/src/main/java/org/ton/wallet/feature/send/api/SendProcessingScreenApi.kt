package org.ton.wallet.feature.send.api

interface SendProcessingScreenApi {

    fun navigateBack()

    fun navigateToMain()

    companion object {

        const val ResultKeyFeeChanged = "feeChanged"
    }
}