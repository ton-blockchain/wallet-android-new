package org.ton.wallet.domain.tonconnect.impl

import android.util.Base64
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.crypto.digest.sha256
import org.ton.wallet.core.ext.toHexString
import org.ton.wallet.core.ext.toUriSafe
import org.ton.wallet.data.core.connect.TonConnect
import org.ton.wallet.data.core.connect.TonConnectRequest
import org.ton.wallet.data.core.link.LinkAction
import org.ton.wallet.data.core.model.TonAccount
import org.ton.wallet.data.core.ton.TonWalletHelper
import org.ton.wallet.data.tonconnect.api.TonConnectRepository
import org.ton.wallet.data.wallet.api.WalletRepository
import org.ton.wallet.data.wallet.api.model.AccountDto
import org.ton.wallet.domain.blockhain.api.GetAddressUseCase
import org.ton.wallet.domain.tonconnect.api.TonConnectOpenConnectionUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class TonConnectOpenConnectionUseCaseImpl(
    private val appVersion: String,
    private val json: Json,
    private val tonConnectRepository: TonConnectRepository,
    private val walletRepository: WalletRepository,
    private val getAddressUseCase: GetAddressUseCase,
    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase
) : TonConnectOpenConnectionUseCase {

    override suspend fun invoke(action: LinkAction.TonConnectAction, manifest: TonConnect.Manifest) {
        val accountDto = getCurrentAccountDataUseCase.getAccountState()
            ?: throw IllegalArgumentException("Current account is null")
        tonConnectRepository.connect(accountDto.id, action.clientId)

        val payloadItems = action.request.items.mapNotNull { connectItem ->
            when (connectItem) {
                TonConnectRequest.ConnectItem.Address -> getAddressItem(accountDto)
                is TonConnectRequest.ConnectItem.Proof -> getProofItem(connectItem, accountDto, manifest)
            }
        }

        val deviceInfo = TonConnect.DeviceInfo(
            platform = TonConnect.PlatformAndroid,
            appVersion = appVersion,
        )

        val payload = TonConnect.ConnectEventSuccess.Payload(payloadItems, deviceInfo)
        val event = TonConnect.ConnectEventSuccess(0, payload)
        val connectEventJson = json.encodeToString(event)
        tonConnectRepository.sendMessage(accountDto.id, action.clientId, connectEventJson.toByteArray())
    }

    private suspend fun getAddressItem(accountDto: AccountDto): TonConnect.ConnectItemReply.ItemAddress {
        val rawAddress = getAddressUseCase.getRawAddress(accountDto.address) ?: ""
        val tonAccount = TonAccount(walletRepository.publicKey, accountDto.version, accountDto.revision)
        val walletStateInit = Base64.encodeToString(TonWalletHelper.getStateInitBytes(tonAccount), Base64.NO_WRAP)
        return TonConnect.ConnectItemReply.ItemAddress(
            address = rawAddress,
            publicKey = tonAccount.getPublicKeyBytes().toHexString(),
            network = TonConnect.NetworkMainNet,
            walletStateInit = walletStateInit
        )
    }

    private suspend fun getProofItem(
        connectItem: TonConnectRequest.ConnectItem.Proof,
        accountDto: AccountDto,
        manifest: TonConnect.Manifest
    ): TonConnect.ConnectItemReply.ItemProof? {
        val msgOutputStream = ByteArrayOutputStream()
        msgOutputStream.write("ton-proof-item-v2/".toByteArray())

        val unpackedAddress = getAddressUseCase.getUnpackedAddress(accountDto.address)
            ?: return null
        msgOutputStream.write(unpackedAddress.first)
        msgOutputStream.write(unpackedAddress.second)

        val appDomain = try {
            manifest.url.toUriSafe()?.host ?: ""
        } catch (e: Exception) {
            ""
        }
        msgOutputStream.write(appDomain.length)
        msgOutputStream.write(appDomain.toByteArray())

        val timestamp = System.currentTimeMillis()
        msgOutputStream.write(ByteBuffer.allocate(8).putLong(timestamp).array())
        connectItem.payload?.toByteArray()?.let(msgOutputStream::write)

        val firstPartByteStream = ByteArrayOutputStream()
        firstPartByteStream.write(0xFFFF)
        firstPartByteStream.write("ton-connect".toByteArray())

        val messageOutputStream = ByteArrayOutputStream()
        messageOutputStream.write(sha256(firstPartByteStream.toByteArray()))
        messageOutputStream.write(sha256(msgOutputStream.toByteArray()))
        val message = messageOutputStream.toByteArray()

        val privateKey = PrivateKeyEd25519(walletRepository.seed)
        val signature = privateKey.sign(message)

        val proof = TonConnect.ConnectItemReply.ItemProof.Proof(
            timestamp = timestamp.toDouble(),
            domain = TonConnect.ConnectItemReply.ItemProof.Proof.Domain(
                lengthBytes = appDomain.length,
                value = appDomain
            ),
            signature = Base64.encodeToString(signature, Base64.NO_WRAP),
            payload = connectItem.payload
        )
        return TonConnect.ConnectItemReply.ItemProof.success(proof)
    }
}