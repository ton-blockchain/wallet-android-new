package org.ton.wallet.lib.log.target

import android.util.Log

internal class LogcatTarget : LogTarget {

    override var isEnabled: Boolean = false

    override fun log(priority: Int, tag: String, msg: String) {
        if (isEnabled) {
            Log.println(priority, tag, msg)
        }
    }
}