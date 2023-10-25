package org.ton.wallet.domain.blockhain.api

typealias UnpackedAddress = Pair<Int, ByteArray>

interface GetAddressUseCase {

    @Throws(Exception::class)
    suspend fun isValidUfAddress(address: String): Boolean

    suspend fun getUfAddress(rawAddress: String): String?

    suspend fun getRawAddress(ufAddress: String): String?

    suspend fun getUnpackedAddress(ufAddress: String): UnpackedAddress?

    @Throws(Exception::class)
    suspend fun resolveDnsName(dnsName: String): String?
}