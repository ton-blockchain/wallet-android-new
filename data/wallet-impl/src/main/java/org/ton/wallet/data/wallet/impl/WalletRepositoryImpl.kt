package org.ton.wallet.data.wallet.impl

import android.content.SharedPreferences
import android.util.Base64
import drinkless.org.ton.TonApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.ton.mnemonic.Mnemonic
import org.ton.wallet.core.ext.clear
import org.ton.wallet.data.core.SecuredPrefsKeys
import org.ton.wallet.data.tonclient.api.TonClient
import org.ton.wallet.data.tonclient.api.sendRequestTyped
import org.ton.wallet.data.wallet.api.WalletRepository
import org.ton.wallet.lib.security.SecurityUtils

class WalletRepositoryImpl(
    private val tonClient: TonClient,
    private val defaultPreferences: SharedPreferences,
    private val securedPreferences: SharedPreferences
) : WalletRepository {

    override val hasWalletFlow = MutableStateFlow(false)

    override val publicKey: String
        get() = securedPreferences.getString(SecuredPrefsKeys.PublicKey, "") ?: ""

    override val password: ByteArray
        get() = Base64.decode(securedPreferences.getString(SecuredPrefsKeys.Password, "") ?: "", Base64.NO_WRAP)

    override val secret: ByteArray
        get() = Base64.decode(securedPreferences.getString(SecuredPrefsKeys.Secret, "") ?: "", Base64.NO_WRAP)

    override val seed: ByteArray
        get() = Mnemonic.toSeed(getRecoveryPhrase())

    init {
        hasWalletFlow.value = securedPreferences.contains(SecuredPrefsKeys.Words)
    }

    @Throws(Exception::class)
    override suspend fun createWallet(words: Array<String>?) {
        val password = SecurityUtils.randomBytesSecured(64)
        val seed = SecurityUtils.randomBytesSecured(32)

        val key: TonApi.Key
        val wordsArray: Array<String>
        if (words == null) {
            key = tonClient.sendRequestTyped(TonApi.CreateNewKey(password, null, seed))
            val exportKeyRequest = TonApi.ExportKey(TonApi.InputKeyRegular(key, password))
            val exportKeyResponse = tonClient.sendRequestTyped<TonApi.ExportedKey>(exportKeyRequest)
            wordsArray = exportKeyResponse.wordList
        } else {
            wordsArray = words
            key = tonClient.sendRequestTyped(TonApi.ImportKey(password, null, TonApi.ExportedKey(wordsArray)))
        }

        securedPreferences.edit()
            .putString(SecuredPrefsKeys.Password, Base64.encodeToString(password, Base64.NO_WRAP))
            .putString(SecuredPrefsKeys.PublicKey, key.publicKey)
            .putString(SecuredPrefsKeys.Secret, Base64.encodeToString(key.secret, Base64.NO_WRAP))
            .putString(SecuredPrefsKeys.Words, wordsArray.joinToString("|"))
            .apply()

        password.clear()
        seed.clear()
        hasWalletFlow.value = true
    }

    override fun getRecoveryPhrase(): List<String> {
        return securedPreferences.getString(SecuredPrefsKeys.Words, "")
            ?.split("|")
            ?: emptyList()
    }

    override suspend fun deleteWallet() {
        hasWalletFlow.value = false
    }
}