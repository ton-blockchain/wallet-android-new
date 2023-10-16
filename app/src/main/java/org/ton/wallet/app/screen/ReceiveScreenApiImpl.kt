package org.ton.wallet.app.screen

import android.app.Activity
import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.app.util.AppIntentUtils
import org.ton.wallet.feature.wallet.api.ReceiveScreenApi

internal class ReceiveScreenApiImpl(
    private val navigator: Navigator
) : ReceiveScreenApi {

    override fun shareText(activity: Activity, text: String, chooserTitle: String) {
        AppIntentUtils.shareText(activity, text, chooserTitle)
    }
}