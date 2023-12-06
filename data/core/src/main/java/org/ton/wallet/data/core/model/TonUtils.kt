package org.ton.wallet.data.core.model

import io.ktor.utils.io.core.ByteReadPacket
import org.ton.crypto.base64
import org.ton.crypto.base64url
import kotlin.experimental.and
import kotlin.experimental.xor

object TonUtils {

    private const val FlagAddressBounceable = 0x11.toByte()
    private const val FlagAddressNonBounceable = 0x51.toByte()
    private const val FlagAddressTest = 0x80.toByte()

    @Throws(IllegalArgumentException::class)
    fun isBounceableAddress(address: String): Boolean {
        // use non-bounceable for raw address
        if (address.contains(":")) {
            return false
        }

        val bytes = try {
            base64url(address)
        } catch (e: Exception) {
            try {
                base64(address)
            } catch (e: Exception) {
                throw IllegalArgumentException("Can't parse address: $address", e)
            }
        }
        val packet = ByteReadPacket(bytes)

        // remote test flag if exist
        var tag = packet.readByte()
        if (tag and FlagAddressTest == FlagAddressTest) {
            tag = tag xor FlagAddressTest
        }

        return when (tag) {
            FlagAddressBounceable -> true
            FlagAddressNonBounceable -> false
            else -> throw IllegalArgumentException("Can't parse address: $address")
        }
    }
}