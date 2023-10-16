package org.ton.wallet.feature.wallet.api

import android.app.Activity

interface ReceiveScreenApi {

    fun shareText(activity: Activity, text: String, chooserTitle: String = "")
}