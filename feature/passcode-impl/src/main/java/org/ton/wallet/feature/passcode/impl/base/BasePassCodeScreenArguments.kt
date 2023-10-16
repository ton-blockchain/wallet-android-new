package org.ton.wallet.feature.passcode.impl.base

import org.ton.wallet.screen.ScreenArguments

interface BasePassCodeScreenArguments : ScreenArguments {

    val isBackVisible: Boolean

    val isDark: Boolean
}