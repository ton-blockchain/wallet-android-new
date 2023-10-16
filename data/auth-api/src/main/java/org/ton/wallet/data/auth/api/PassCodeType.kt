package org.ton.wallet.data.auth.api

enum class PassCodeType(val rawValue: Int) {
    Pin4(4),
    Pin6(6);

    companion object {

        fun fromRawValue(rawValue: Int): PassCodeType? {
            return entries.firstOrNull { it.rawValue == rawValue }
        }
    }
}