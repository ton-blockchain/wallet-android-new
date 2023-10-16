package org.ton.wallet.core.ext

private val HexDigits = "0123456789abcdef".toCharArray()

fun ByteArray.toHexString(): String {
    val stringBuilder = stringBuilder.getSafe().clear()
    for (i in indices) {
        val b = this[i].toInt() and 0xFF
        stringBuilder.append(HexDigits[b shr 4])
        stringBuilder.append(HexDigits[b and 0x0F])
    }
    return stringBuilder.toString()
}

fun String.toHexByteArray(): ByteArray {
    val result = ByteArray(this.length / 2)
    for (idx in result.indices) {
        val charPosition = idx * 2
        val high = this[charPosition].toString().toInt(16) shl 4
        val low = this[charPosition + 1].toString().toInt(16)
        result[idx] = (high or low).toByte()
    }
    return result
}