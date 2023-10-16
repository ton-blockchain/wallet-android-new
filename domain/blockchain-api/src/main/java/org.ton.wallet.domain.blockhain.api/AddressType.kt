package org.ton.wallet.domain.blockhain.api

sealed class AddressType(val ufAddress: String?) {

    class DnsAddress(val dns: String, ufAddress: String?) : AddressType(ufAddress)

    class RawAddress(val raw: String, ufAddress: String?) : AddressType(ufAddress)

    class UserFriendlyAddress(ufAddress: String) : AddressType(ufAddress)
}