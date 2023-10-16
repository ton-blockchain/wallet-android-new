package org.ton.wallet.feature.passcode.impl.enter

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.wallet.data.auth.api.PassCodeType
import org.ton.wallet.feature.passcode.impl.base.BasePassCodeScreenArguments
import org.ton.wallet.screen.AppScreen

@Parcelize
class PassCodeEnterScreenArguments(
    val purpose: String,
    val passCodeType: PassCodeType? = null,
    val isOnlyPassCode: Boolean = false,
    val isPassCodeToResult: Boolean = false,
    override val isBackVisible: Boolean = true,
    override val isDark: Boolean = false,
) : BasePassCodeScreenArguments, Parcelable {

    @IgnoredOnParcel
    override val screen: String = AppScreen.PassCodeEnter.name
}