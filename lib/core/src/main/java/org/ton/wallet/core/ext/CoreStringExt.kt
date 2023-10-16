package org.ton.wallet.core.ext

import android.net.Uri
import android.text.SpannableStringBuilder

internal val stringBuilder = threadLocalSafe { StringBuilder() }

fun SpannableStringBuilder.addSpans(spans: List<Any>, start: Int, end: Int, flags: Int) {
    spans.forEach { span ->
        setSpan(span, start, end, flags)
    }
}

fun String.hiddenMiddle(start: Int, end: Int): String {
    if (length <= start + end) {
        return this
    }
    return stringBuilder.getSafe().clear()
        .append(this.substring(0, start))
        .append("...")
        .append(this.substring(length - end, length))
        .toString()
}

fun String.toUriSafe(): Uri? {
    return try {
        Uri.parse(this)
    } catch (e: Exception) {
        null
    }
}