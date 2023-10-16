package org.ton.wallet.feature.tonconnect.impl

import androidx.annotation.IntDef

data class TonConnectApproveState(
    val isDataLoading: Boolean,
    val accountAddress: String = "",
    val accountVersion: String = "",
    val appName: String = "",
    val appIconUrl: String = "",
    val appHost: String = "",
    @ConnectionState
    val connectionState: Int = ConnectionDefault
) {

    companion object {

        @IntDef(ConnectionDefault, ConnectionInProgress, ConnectionConnected)
        @Retention(AnnotationRetention.SOURCE)
        annotation class ConnectionState
        const val ConnectionDefault = 0
        const val ConnectionInProgress = 1
        const val ConnectionConnected = 2
    }
}