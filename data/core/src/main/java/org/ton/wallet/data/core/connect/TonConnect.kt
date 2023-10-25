package org.ton.wallet.data.core.connect

import androidx.annotation.StringDef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// https://github.com/ton-blockchain/ton-connect
// https://github.com/ton-blockchain/ton-connect/blob/main/requests-responses.md#initiating-connection
// https://github.com/ton-blockchain/ton-connect/blob/main/bridge.md
// https://ton-connect.github.io/demo-dapp/
class TonConnect {

    @Serializable
    class Manifest(
        val url: String,
        val name: String,
        val iconUrl: String,
        val termsOfUseUrl: String? = null,
        val privacyPolicyUrl: String? = null,
    )

    @Serializable
    class ConnectEventSuccess<T>(
        val id: Int,
        val payload: ConnectEventSuccess.Payload<T>,
        val event: String = "connect"
    ) {

        @Serializable
        class Payload<T>(
            val items: List<T>,
            val device: DeviceInfo
        )
    }

    @Serializable
    class ConnectEventError(
        val id: Int,
        val name: String = "error"
    )


    @Serializable
    class DeviceInfo(
        val platform: String,
        val appVersion: String,
        val appName: String = "TON Wallet",
        val maxProtocolVersion: Int = 2,
        val features: List<String> = listOf("SendTransaction", "SignData")
    )

    @Serializable
    sealed interface ConnectItemReply {

        @Serializable
        class ItemAddress(
            // raw address
            val address: String,
            @NetworkType
            val network: String,
            val publicKey: String,
            val walletStateInit: String,
            val name: String = ConnectItemNameTonAddress
        ) : ConnectItemReply

        @Serializable
        class ItemProof private constructor(
            val proof: Proof? = null,
            val error: Error? = null,
            val name: String = ConnectItemNameTonProof
        ) : ConnectItemReply {

            companion object {

                fun success(proof: Proof): ItemProof {
                    return ItemProof(proof = proof)
                }

                fun error(error: Error): ItemProof {
                    return ItemProof(error = error)
                }
            }

            @Serializable
            class Proof(
                val timestamp: Double,
                val domain: Domain,
                val signature: String,
                val payload: String?
            ) {

                @Serializable
                class Domain(
                    val lengthBytes: Int,
                    val value: String
                )
            }
        }
    }

    @Serializable
    class BridgeMessage(
        val from: String,
        val message: String
    )

    @Serializable
    class AppRawRequest(
        val id: Int,
        val method: String,
        val params: List<String>
    ) {

        @Serializable
        class Payload(
            val messages: List<Param>,
            @SerialName("valid_until")
            val validUntil: Long,
        )

        @Serializable
        class Param(
            val address: String,
            val amount: Long,
            val payload: String?,
            val stateInit: String?
        )
    }


    sealed class SendTransactionResponse {

        @Serializable
        class Success(
            val id: String,
            val boc: String = ""
        ) : SendTransactionResponse()

        @Serializable
        class Error(
            val id: String,
            val error: TonConnect.Error
        ) : SendTransactionResponse()
    }


    @Serializable
    class Error(
        val code: Int,
        val message: String
    )


    sealed class Ret {
        object Back : Ret()
        object None : Ret()
        class Url(val url: String) : Ret()
    }


    companion object {

        const val ConnectItemNameTonAddress = "ton_addr"
        const val ConnectItemNameTonProof = "ton_proof"

        @StringDef(NetworkMainNet, NetworkTestNet)
        @Retention(AnnotationRetention.SOURCE)
        annotation class NetworkType
        const val NetworkMainNet = "-239"
        const val NetworkTestNet = "-3"

        const val PlatformAndroid = "android"
        const val PlatformIPad = "ipad"
        const val PlatformIPhone = "iphone"
        const val PlatformLinux = "linux"
        const val PlatformMac = "mac"
        const val PlatformWindows = "windows"
    }

}