package org.ton.wallet.lib.tonconnect

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.wallet.lib.tonconnect.TonConnectApi.ConnectRequest.ConnectItem.Companion.NameTonAddress
import org.ton.wallet.lib.tonconnect.TonConnectApi.ConnectRequest.ConnectItem.Companion.NameTonProof

/**
 * https://github.com/ton-blockchain/ton-connect/blob/main/requests-responses.md
 */
interface TonConnectApi {

    @Serializable
    class AppManifest(
        val url: String,
        val name: String,
        val iconUrl: String,
        val termsOfUseUrl: String? = null,
        val privacyPolicyUrl: String? = null,
    )

    interface AppMessage

    @Serializable
    data class AppRequest(
        val id: Long,
        val method: String,
        val params: List<String>
    ): AppMessage {

        companion object {

            const val MethodSendTransaction = "sendTransaction"
            const val MethodSignData = "signData"
            const val MethodDisconnect = "disconnect"
        }
    }

    interface AppRequestEvent : Parcelable

    @Parcelize
    @Serializable
    data class SendTransactionRequest(
        val messages: List<Message>,
        val network: String?,
        val from: String?,
        @SerialName("valid_until")
        val validUntil: Long?
    ): AppRequestEvent {

        @Parcelize
        @Serializable
        data class Message(
            val address: String,
            val amount: String,
            val payload: String?,
            val stateInit: String?
        ) : Parcelable
    }

    @Parcelize
    class DisconnectRequest: AppRequestEvent

    @Serializable
    class SendTransactionResponse private constructor(
        val id: Long,
        val result: String?,
        val error: Error?
    ) {

        companion object {

            fun createSuccess(id: Long, boc: String): SendTransactionResponse {
                return SendTransactionResponse(id, boc, null)
            }

            fun createError(id: Long, code: Int, message: String): SendTransactionResponse {
                return SendTransactionResponse(id, null, Error(code, message))
            }
        }
    }

    @Serializable
    class ConnectRequest(
        val manifestUrl: String,
        val items: List<ConnectItem>
    ) : AppMessage {

        @Serializable
        class ConnectItem(
            val name: String,
            val payload: String?
        ) {

            companion object {

                const val NameTonAddress = "ton_addr"
                const val NameTonProof = "ton_proof"
            }
        }
    }


    interface WalletMessage

    @Serializable
    class WalletResponse private constructor(
        val id: String,
        val result: String?,
        val error: Error?
    ) : WalletMessage {

        companion object {

            fun success(id: String, result: String): WalletResponse {
                return WalletResponse(id, result, null)
            }

            fun error(id: String, code: Int, message: String): WalletResponse {
                return WalletResponse(id, null, Error(code, message))
            }
        }
    }


    interface WalletEvent : WalletMessage

    @Serializable
    class ConnectEvent private constructor(
        val event: String,
        val id: Long,
        val payload: ConnectEventPayload,
    ) : WalletEvent {

        companion object {

            fun createConnectSuccess(id: Long, items: List<ConnectItemReply>, device: Device): ConnectEvent {
                return ConnectEvent("connect", id, ConnectEventPayload.createSuccess(items, device))
            }

            fun createConnectError(id: Long, code: Int, message: String): ConnectEvent {
                return ConnectEvent("connect_error", id, ConnectEventPayload.createError(code, message))
            }

            fun createDisconnect(id: Long): ConnectEvent {
                return ConnectEvent("disconnect", id, ConnectEventPayload.createEmpty())
            }
        }

        @Serializable
        class ConnectEventPayload private constructor(
            val items: List<ConnectItemReply>? = null,
            val device: Device? = null,
            val code: Int? = null,
            val message: String? = null,
        ) {

            companion object {

                fun createSuccess(items: List<ConnectItemReply>, device: Device): ConnectEventPayload {
                    return ConnectEventPayload(items = items, device = device)
                }

                fun createError(code: Int, message: String): ConnectEventPayload {
                    return ConnectEventPayload(code = code, message = message)
                }

                fun createEmpty(): ConnectEventPayload {
                    return ConnectEventPayload()
                }
            }
        }

        @Serializable
        class ConnectItemReply private constructor(
            val name: String,

            // TonAddressItemReply
            val address: String? = null,
            val network: String? = null,
            val publicKey: String? = null,
            val walletStateInit: String? = null,

            // TonProofItemReplySuccess
            val proof: TonProof? = null,

            // TonProofItemReplyError
            val error: Error? = null
        ) {

            companion object {

                fun createAddress(address: String, network: String, publicKey: String, stateInit: String): ConnectItemReply {
                    return ConnectItemReply(
                        name = NameTonAddress,
                        address = address,
                        network = network,
                        publicKey = publicKey,
                        walletStateInit = stateInit
                    )
                }

                fun createProofSuccess(proof: TonProof): ConnectItemReply {
                    return ConnectItemReply(name = NameTonProof, proof = proof)
                }

                fun createProofError(error: Error): ConnectItemReply {
                    return ConnectItemReply(name = NameTonProof, error = error)
                }
            }

            @Serializable
            class TonProof(
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

        @Serializable
        class Device(
            val platform: String,
            val appName: String,
            val appVersion: String,
            val maxProtocolVersion: Int,
            val features: List<Feature>
        )

        @Serializable
        class Feature(
            val name: String,
            val maxMessages: Int? = null
        ) {

            companion object {

                const val FeatureSendTransaction = "SendTransaction"
                const val FeatureSignData = "SignData"
            }
        }
    }

    @Serializable
    class Error(
        val code: Int,
        val message: String?
    )

    @Serializable
    class BridgeMessage(
        val from: String,
        val message: String
    )


    companion object {

        const val ErrorCodeUnknown = 0
        const val ErrorCodeBadRequest = 1
        const val ErrorCodeAppManifestNotFound = 2
        const val ErrorCodeAppManifestContentError = 3
        const val ErrorCodeUnknownApp = 100
        const val ErrorCodeUserDeclinedConnection = 300
        const val ErrorCodeMethodNotSupported = 400

        const val ErrorMessageNetworkNotMainnet = "Network is not Mainnet"
        const val ErrorMessageRequestExpired = "Request expired"
        const val ErrorMessageUserDeclinedConnection = "User declined the transaction"

        const val NetworkMainnet = "-239"
        const val NetworkTestnet = "-3"
    }
}