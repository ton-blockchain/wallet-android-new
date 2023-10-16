package org.ton.wallet.app.util

import java.security.KeyStore

object AppKeystoreUtils {

    const val AndroidKeyStoreName = "AndroidKeyStore"

    val keyStore: KeyStore = KeyStore.getInstance(AndroidKeyStoreName)

    fun init() {
        keyStore.load(null)
    }
}