package org.ton.wallet.core.ext

import java.util.Arrays

fun ByteArray.clear() {
    Arrays.fill(this, 0)
}

fun ByteArray.toIntOrNull(): Int? {
    if (size != 4) {
        return null
    }
    return (this[0].toInt() and 0xFF shl 24) or
            (this[1].toInt() and 0xFF shl 16) or
            (this[2].toInt() and 0xFF shl 8) or
            (this[3].toInt() and 0xFF)
}