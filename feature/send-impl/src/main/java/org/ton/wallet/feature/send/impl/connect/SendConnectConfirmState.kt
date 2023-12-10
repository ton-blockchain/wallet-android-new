package org.ton.wallet.feature.send.impl.connect

class SendConnectConfirmState(
    val adapterItems: List<Any>,
    val dataState: DataState,
) {

    enum class DataState {
        Loading,
        Default,
        Sending,
        Sent
    }
}