package org.ton.wallet.lib.tonconnect

import androidx.annotation.Keep

@Keep
object TonConnectCrypto {

    init {
        System.loadLibrary("tonconnectcrypto")
    }

    @Keep
    external fun nativeCryptoBoxInitKeys(): ByteArray?

    @Keep
    external fun nativeCryptoBox(message: ByteArray?, publicKey: ByteArray?, secretKey: ByteArray?): ByteArray?

    @Keep
    external fun nativeCryptoBoxOpen(cipher: ByteArray?, publicKey: ByteArray?, secretKey: ByteArray?): ByteArray?
}