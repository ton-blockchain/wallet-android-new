package org.ton.wallet.core.ext

fun Int.toByteArrayBigEndian(): ByteArray {
    return byteArrayOf(
        shr(24).and(0xFF).toByte(),
        shr(16).and(0xFF).toByte(),
        shr(8).and(0xFF).toByte(),
        and(0xFF).toByte(),
    )
}

fun Int.toByteArrayLittleEndian(): ByteArray {
    return byteArrayOf(
        and(0xFF).toByte(),
        shr(8).and(0xFF).toByte(),
        shr(16).and(0xFF).toByte(),
        shr(24).and(0xFF).toByte(),
    )
}

fun Long.toByteArrayBigEndian(): ByteArray {
    return byteArrayOf(
        shr(56).and(0xFF).toByte(),
        shr(48).and(0xFF).toByte(),
        shr(40).and(0xFF).toByte(),
        shr(32).and(0xFF).toByte(),
        shr(24).and(0xFF).toByte(),
        shr(16).and(0xFF).toByte(),
        shr(8).and(0xFF).toByte(),
        and(0xFF).toByte(),
    )
}

fun Long.toByteArrayLittleEndian(): ByteArray {
    return byteArrayOf(
        and(0xFF).toByte(),
        shr(8).and(0xFF).toByte(),
        shr(16).and(0xFF).toByte(),
        shr(24).and(0xFF).toByte(),
        shr(32).and(0xFF).toByte(),
        shr(40).and(0xFF).toByte(),
        shr(48).and(0xFF).toByte(),
        shr(56).and(0xFF).toByte(),
    )
}