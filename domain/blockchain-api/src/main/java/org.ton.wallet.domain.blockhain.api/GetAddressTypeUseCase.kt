package org.ton.wallet.domain.blockhain.api

interface GetAddressTypeUseCase {

    suspend fun getAddressType(input: String): AddressType?

    fun guessAddressType(input: String): AddressType?
}