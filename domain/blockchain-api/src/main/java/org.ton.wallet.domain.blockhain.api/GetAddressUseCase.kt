package org.ton.wallet.domain.blockhain.api

interface GetAddressUseCase {

    @Throws(Exception::class)
    suspend fun isValidUfAddress(address: String): Boolean

    @Throws(Exception::class)
    suspend fun getUfAddress(rawAddress: String): String?

    @Throws(Exception::class)
    suspend fun getRawAddress(ufAddress: String): String?

    @Throws(Exception::class)
    suspend fun resolveDnsName(dnsName: String): String?
}