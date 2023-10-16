package org.ton.wallet.lib.security

import androidx.annotation.Keep
import java.security.SecureRandom

@Keep
object SecurityUtils {

    private const val ArgonHashSize = 32
    private const val ArgonIterations = 10000
    private const val ArgonMemoryTwoDegree = 12
    private const val ArgonParallelism = 1

    private val secureRandom by lazy { SecureRandom() }

    init {
        System.loadLibrary("tonsecurity")
    }

    fun getArgonHash(password: ByteArray, salt: ByteArray): ByteArray? {
        return nativeGetArgonHash(password, salt, ArgonIterations, ArgonMemoryTwoDegree, ArgonParallelism, ArgonHashSize)
    }

    fun randomBytesSecured(length: Int): ByteArray {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return bytes
    }

    @Keep
    external fun nativeCryptoBoxInitKeys(): ByteArray?

    @Keep
    external fun nativeCryptoBox(message: ByteArray?, publicKey: ByteArray?, secretKey: ByteArray?): ByteArray?

    @Keep
    external fun nativeCryptoBoxOpen(cipher: ByteArray?, publicKey: ByteArray?, secretKey: ByteArray?): ByteArray?

    @Keep
    private external fun nativeGetArgonHash(password: ByteArray?, salt: ByteArray?, tCost: Int, mCost: Int, parallelism: Int, hashLen: Int): ByteArray?
}