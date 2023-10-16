package org.ton.wallet.feature.passcode.impl.setup

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.feature.passcode.impl.base.BasePassCodeScreenArguments
import org.ton.wallet.screen.AppScreen

@Parcelize
class PassCodeSetupScreenArguments(
    override val isBackVisible: Boolean = true,
    override val isDark: Boolean = false,
    val passCode: String? = null,
    val withBiometrics: Boolean = true
) : BasePassCodeScreenArguments, Parcelable {

    @IgnoredOnParcel
    override val screen: String = AppScreen.PassCodeSetup.name
}