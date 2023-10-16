package org.ton.wallet.domain.tonconnect.impl

import android.util.Base64
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ton.wallet.core.ext.toHexString
import org.ton.wallet.data.core.connect.TonConnect
import org.ton.wallet.data.core.link.LinkAction
import org.ton.wallet.data.core.model.TonAccount
import org.ton.wallet.data.core.ton.TonWalletHelper
import org.ton.wallet.data.tonconnect.api.TonConnectRepository
import org.ton.wallet.data.wallet.api.WalletRepository
import org.ton.wallet.domain.blockhain.api.GetAddressUseCase
import org.ton.wallet.domain.tonconnect.api.TonConnectOpenConnectionUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase

class TonConnectOpenConnectionUseCaseImpl(
    private val appVersion: String,
    private val json: Json,
    private val tonConnectRepository: TonConnectRepository,
    private val walletRepository: WalletRepository,
    private val getAddressUseCase: GetAddressUseCase,
    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase
) : TonConnectOpenConnectionUseCase {

    override suspend fun invoke(action: LinkAction.TonConnectAction) {
        val accountDto = getCurrentAccountDataUseCase.getAccountState()
            ?: throw IllegalArgumentException("Current account is null")
        tonConnectRepository.connect(accountDto.id, action.clientId)

        val rawAddress = getAddressUseCase.getRawAddress(accountDto.address) ?: ""

        val tonAccount = TonAccount(walletRepository.publicKey, accountDto.version, accountDto.revision)
        val walletStateInit = Base64.encodeToString(TonWalletHelper.getStateInitBytes(tonAccount), Base64.NO_WRAP)
        val payload = TonConnect.ConnectEvent.Success.Payload(
            items = listOf(
                TonConnect.TonAddress(
                    address = rawAddress,
                    publicKey = tonAccount.getPublicKeyBytes().toHexString(),
                    network = TonConnect.NetworkMainNet,
                    walletStateInit = walletStateInit
                )
            ),
            device = TonConnect.DeviceInfo(
                platform = TonConnect.PlatformAndroid,
                appVersion = appVersion,
            )
        )
        val event = TonConnect.ConnectEvent.Success(0, payload)
        val connectEventJson = json.encodeToString(event)
        tonConnectRepository.sendMessage(accountDto.id, action.clientId, connectEventJson.toByteArray())
    }
}