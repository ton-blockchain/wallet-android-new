package org.ton.wallet.lib.log.target

internal interface LogTarget {

    var isEnabled: Boolean

    fun log(priority: Int, tag: String, msg: String)
}