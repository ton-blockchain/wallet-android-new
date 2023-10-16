package org.ton.wallet.screen.controller

import android.os.Bundle

interface TargetResultHandler {

    fun onResultReceived(code: String, args: Bundle?) = Unit

    fun setTargetResult(code: String, args: Bundle?) = Unit
}