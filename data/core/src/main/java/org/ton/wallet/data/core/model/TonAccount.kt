package org.ton.wallet.data.core.model

import android.util.Base64

class TonAccount(
    val publicKeyBase64: String,
    val version: Int,
    val revision: Int,
    var subWalletId: Int = DefaultWalletId,
    var seqNo: Int = 0
) {

    val type = TonAccountType.getAccountType(version, revision)

    fun getPublicKeyBytes(): ByteArray {
        return Base64.decode(publicKeyBase64, Base64.URL_SAFE).copyOfRange(2, 34)
    }

    private companion object {

        private const val DefaultWalletId = 698983191
    }
}