package org.ton.wallet.screen

import android.os.*

interface ScreenArguments : Parcelable {

    val screen: String

    companion object {

        const val BUNDLE_KEY_ARGUMENTS = "arguments"
    }
}

inline fun <reified T : ScreenArguments> Bundle?.getScreenArguments(): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this?.getParcelable(ScreenArguments.BUNDLE_KEY_ARGUMENTS, T::class.java)
    } else {
        this?.getParcelable(ScreenArguments.BUNDLE_KEY_ARGUMENTS)
    } ?: throw IllegalArgumentException("Arguments not found")
}