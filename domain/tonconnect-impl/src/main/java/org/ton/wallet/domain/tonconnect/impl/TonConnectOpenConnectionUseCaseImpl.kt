package org.ton.wallet.domain.tonconnect.impl

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.crypto.base64
import org.ton.crypto.digest.sha256
import org.ton.wallet.core.ext.*
import org.ton.wallet.data.core.connect.TonConnectRequest
import org.ton.wallet.data.core.link.LinkAction
import org.ton.wallet.data.core.model.TonAccount
import org.ton.wallet.data.wallet.api.WalletRepository
import org.ton.wallet.data.wallet.api.model.AccountDto
import org.ton.wallet.domain.blockhain.api.GetAddressUseCase
import org.ton.wallet.domain.tonconnect.api.TonConnectOpenConnectionUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase
import org.ton.wallet.lib.tonconnect.TonConnectApi
import org.ton.wallet.lib.tonconnect.TonConnectClient
import java.io.ByteArrayOutputStream

class TonConnectOpenConnectionUseCaseImpl(
    private val appVersion: String,
    private val json: Json,
    private val tonConnectClient: TonConnectClient,
    private val walletRepository: WalletRepository,
    private val getAddressUseCase: GetAddressUseCase,
    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase
) : TonConnectOpenConnectionUseCase {

    override suspend fun invoke(action: LinkAction.TonConnectAction, manifest: TonConnectApi.AppManifest) {
        val accountDto = getCurrentAccountDataUseCase.getAccountState()
            ?: throw IllegalArgumentException("Current account is null")
        tonConnectClient.connect(action.clientId)

        val payloadItems = action.request.items.map { connectItem ->
            when (connectItem) {
                TonConnectRequest.ConnectItem.Address -> {
                    getAddressItem(accountDto)
                }
                is TonConnectRequest.ConnectItem.Proof -> {
                    getProofItem(connectItem, accountDto, manifest)
                }
                is TonConnectRequest.ConnectItem.UnknownMethod -> {
                    val error = TonConnectApi.Error(
                        code = TonConnectApi.ErrorCodeMethodNotSupported,
                        message = "Method ${connectItem.method} is not supported"
                    )
                    TonConnectApi.ConnectEvent.ConnectItemReply.createProofError(error)
                }
            }
        }

        val deviceInfo = TonConnectApi.ConnectEvent.Device(
            platform = "android",
            appName = "TON Wallet",
            appVersion = appVersion,
            maxProtocolVersion = 2,
            features = listOf(
                TonConnectApi.ConnectEvent.Feature(
                    name = TonConnectApi.ConnectEvent.Feature.FeatureSendTransaction,
                    maxMessages = 4
                )
            )
        )
        val connectEventSuccess = TonConnectApi.ConnectEvent.createConnectSuccess(id = 0, items = payloadItems, device = deviceInfo)
        val connectEventJson = json.encodeToString(connectEventSuccess)
        tonConnectClient.sendMessage(action.clientId, connectEventJson)
    }

    private suspend fun getAddressItem(accountDto: AccountDto): TonConnectApi.ConnectEvent.ConnectItemReply {
        val account = TonAccount(walletRepository.publicKey, accountDto.version, accountDto.revision)
        val rawAddress = getAddressUseCase.getRawAddress(accountDto.address) ?: ""
        return TonConnectApi.ConnectEvent.ConnectItemReply.createAddress(
            address = rawAddress,
            network = TonConnectApi.NetworkMainnet,
            publicKey = walletRepository.publicKey,
            stateInit = base64(account.getStateInitBytes())
        )
    }

    private suspend fun getProofItem(
        connectItem: TonConnectRequest.ConnectItem.Proof,
        accountDto: AccountDto,
        manifest: TonConnectApi.AppManifest
    ): TonConnectApi.ConnectEvent.ConnectItemReply {
        val unpackedAddress = getAddressUseCase.getUnpackedAddress(accountDto.address)
        if (unpackedAddress == null) {
            val error = TonConnectApi.Error(
                code = TonConnectApi.ErrorCodeUnknown,
                message = "Could not get unpacked address"
            )
            return TonConnectApi.ConnectEvent.ConnectItemReply.createProofError(error)
        }

        val appDomain = try {
            manifest.url.toUriSafe()?.host
        } catch (e: Exception) {
            null
        }
        if (appDomain == null) {
            val error = TonConnectApi.Error(
                code = TonConnectApi.ErrorCodeUnknown,
                message = "Bad manifest url: ${manifest.url}"
            )
            return TonConnectApi.ConnectEvent.ConnectItemReply.createProofError(error)
        }

        val msgOutputStream = ByteArrayOutputStream()
        msgOutputStream.write("ton-proof-item-v2/".toByteArray())

        msgOutputStream.write(unpackedAddress.first.toByteArrayBigEndian())
        msgOutputStream.write(unpackedAddress.second)

        val appDomainBytes = appDomain.toByteArray()
        msgOutputStream.write(appDomainBytes.size.toByteArrayLittleEndian())
        msgOutputStream.write(appDomainBytes)

        val timestamp = System.currentTimeMillis() / 1000
        msgOutputStream.write(timestamp.toByteArrayLittleEndian())
        connectItem.payload?.toByteArray()?.let(msgOutputStream::write)

        val messageOutputStream = ByteArrayOutputStream()
        messageOutputStream.write(byteArrayOf(0xFF.toByte(), 0xFF.toByte()))
        messageOutputStream.write("ton-connect".toByteArray())
        messageOutputStream.write(sha256(msgOutputStream.toByteArray()))

        val signature = PrivateKeyEd25519(walletRepository.seed).sign(sha256(messageOutputStream.toByteArray()))

        return try {
            val proof = TonConnectApi.ConnectEvent.ConnectItemReply.TonProof(
                timestamp = timestamp.toDouble(),
                domain = TonConnectApi.ConnectEvent.ConnectItemReply.TonProof.Domain(
                    lengthBytes = appDomainBytes.size,
                    value = appDomain
                ),
                signature = base64(signature),
                payload = connectItem.payload
            )
            TonConnectApi.ConnectEvent.ConnectItemReply.createProofSuccess(proof)
        } catch (e: Exception) {
            val error = TonConnectApi.Error(
                code = TonConnectApi.ErrorCodeUnknown,
                message = "Could not create proof"
            )
            TonConnectApi.ConnectEvent.ConnectItemReply.createProofError(error)
        }
    }
}