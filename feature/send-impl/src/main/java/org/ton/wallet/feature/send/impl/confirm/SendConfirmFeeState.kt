package org.ton.wallet.feature.send.impl.confirm

sealed class SendConfirmFeeState {

    object Loading : SendConfirmFeeState()

    object Error : SendConfirmFeeState()

    class Value(val fee: String) : SendConfirmFeeState()
}