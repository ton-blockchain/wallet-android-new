package org.ton.wallet.core.ext

import java.util.Arrays

fun ByteArray.clear() {
    Arrays.fill(this, 0)
}