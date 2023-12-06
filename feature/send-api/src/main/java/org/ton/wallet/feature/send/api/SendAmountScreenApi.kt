package org.ton.wallet.feature.send.api

interface SendAmountScreenApi {

    fun navigateBack()

    fun navigateToConfirm(address: String, amount: Long, isAllAmount: Boolean, message: String?)
}