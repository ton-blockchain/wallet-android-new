package org.ton.wallet.domain.blockhain.impl

import drinkless.org.ton.TonApi
import org.ton.wallet.core.ext.toHexByteArray
import org.ton.wallet.core.ext.toHexString
import org.ton.wallet.data.tonclient.api.*
import org.ton.wallet.domain.blockhain.api.UnpackedAddress

class GetAddressUseCaseImpl(
    private val tonClient: TonClient
) : org.ton.wallet.domain.blockhain.api.GetAddressUseCase {

    override suspend fun isValidUfAddress(address: String): Boolean {
        return try {
            val response = tonClient.sendRequest(TonApi.UnpackAccountAddress(address))
            response is TonApi.UnpackedAccountAddress
        } catch (e: TonApiException) {
            if (e.error.message.startsWith("INVALID_ACCOUNT_ADDRESS")) {
                false
            } else {
                throw e
            }
        }
    }

    override suspend fun getUfAddress(rawAddress: String, isBounceable: Boolean): String? {
        return try {
            val splits = rawAddress.split(':')
            val workChainId = splits[0].toIntOrNull() ?: return null
            val addressBytes = splits[1].toHexByteArray()
            val unpackedAddress = TonApi.UnpackedAccountAddress(workChainId, isBounceable, false, addressBytes)
            val response = tonClient.sendRequestTyped<TonApi.AccountAddress>(TonApi.PackAccountAddress(unpackedAddress))
            return response.accountAddress
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getRawAddress(ufAddress: String): String? {
        return try {
            val unpackedAddress = tonClient.sendRequestTyped<TonApi.UnpackedAccountAddress>(TonApi.UnpackAccountAddress(ufAddress))
            return "${unpackedAddress.workchainId}:${unpackedAddress.addr.toHexString()}"
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUnpackedAddress(ufAddress: String): UnpackedAddress? {
        return try {
            val unpackedAddress = tonClient.sendRequestTyped<TonApi.UnpackedAccountAddress>(TonApi.UnpackAccountAddress(ufAddress))
            unpackedAddress.workchainId to unpackedAddress.addr
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun resolveDnsName(dnsName: String): String? {
        val request = TonApi.DnsResolve(null, dnsName, CategoryEmpty, 5)
        val response = tonClient.sendRequestTyped<TonApi.DnsResolved>(request)
        val entryData = response.entries.firstOrNull { it.category.contentEquals(CategoryWallet) }?.entry
        if (entryData is TonApi.DnsEntryDataSmcAddress) {
            return entryData.smcAddress.accountAddress
        }
        return null
    }

    private companion object {

        private val CategoryEmpty = ByteArray(32) { 0 }
        private val CategoryWallet = byteArrayOf(-24, -44, 64, 80, -121, 61, -70, -122, 90, -89, -63, 112, -85, 76, -50, 100, -39, 8, 57, -93, 77, -49, -42, -49, 113, -47, 78, 2, 5, 68, 59, 27)
    }
}