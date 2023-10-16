package org.ton.wallet.core.ext

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

fun Context.toActivitySafe(): Activity? {
    var context = this
    while (context !is Activity && context is ContextWrapper) {
        context = context.baseContext
    }
    return context as? Activity
}