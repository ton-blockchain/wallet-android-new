package org.ton.wallet.core.ext

import org.json.JSONException
import org.json.JSONObject

fun JSONObject.getStringOrNull(key: String): String? {
    return if (has(key)) {
        try {
            getString(key)
        } catch (e: JSONException) {
            null
        }
    } else {
        null
    }
}