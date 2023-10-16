package org.ton.wallet.feature.wallet.impl.main

class MainScreenState(
    val address: String,
    val tonBalance: Long?,
    val fiatBalanceString: String,
    val headerState: MainScreenHeaderState,
)

enum class MainScreenHeaderState {
    Connecting,
    Default,
    Updating,
    WaitingNetwork
}