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

    sealed class ConnectEvent {

        @Serializable
        class Success(
            val id: Int,
            val payload: Payload,
            val event: String = "connect"
        ) : ConnectEvent() {

            @Serializable
            class Payload(
                val items: List<TonAddress>,
                val device: DeviceInfo
            )
        }

        @Serializable
        class Error(
            val id: Int,
            val name: String = "error"
        ) : ConnectEvent()
    }


    @Serializable
    class DeviceInfo(
        val platform: String,
        val appVersion: String,
        val appName: String = "TON Wallet",
        val maxProtocolVersion: Int = 2,
        val features: List<String> = listOf("SendTransaction", "SignData")
    )

    @Serializable
    class TonAddress(
        // raw address
        val address: String,
        @NetworkType
        val network: String,
        val publicKey: String,
        val walletStateInit: String,
        val name: String = "ton_addr"
    )

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