package org.ton.wallet.lib.tonconnect

import android.content.SharedPreferences
import android.util.Base64

internal class TonConnectStore(
    private val sharedPreferences: SharedPreferences,
    private val securedPreferences: SharedPreferences
) {

    fun getKeys(clientId: String): Pair<ByteArray, ByteArray>? {
        val publicKey = sharedPreferences.getString(getPublicKeyKey(clientId), null)?.let { Base64.decode(it, Base64.NO_WRAP) } ?: return null
        val secretKey = securedPreferences.getString(getSecretKeyKey(clientId), null)?.let { Base64.decode(it, Base64.NO_WRAP) } ?: return null
        return publicKey to secretKey
    }

    fun getSavedClientIds(): Set<String> {
        return sharedPreferences.getStringSet(PrefKeyClientIds, emptySet()) ?: emptySet()
    }

    fun getLastRequestId(clientId: String): Long {
        return sharedPreferences.getLong(getLastRequestIdKey(clientId), -1)
    }

    fun saveLastRequestId(clientId: String, requestId: Long) {
        sharedPreferences.edit().putLong(getLastRequestIdKey(clientId), requestId).apply()
    }

    fun saveConnection(clientId: String, publicKey: ByteArray, secretKey: ByteArray) {
        val clientIds = HashSet(sharedPreferences.getStringSet(PrefKeyClientIds, emptySet()) ?: emptySet())
        clientIds.add(clientId)
        sharedPreferences.edit()
            .putStringSet(PrefKeyClientIds, clientIds)
            .putString(getPublicKeyKey(clientId), Base64.encodeToString(publicKey, Base64.NO_WRAP))
            .apply()
        securedPreferences.edit()
            .putString(getSecretKeyKey(clientId), Base64.encodeToString(secretKey, Base64.NO_WRAP))
            .apply()
    }

    fun removeConnection(clientId: String) {
        val clientIds = HashSet(sharedPreferences.getStringSet(PrefKeyClientIds, emptySet()) ?: emptySet())
        clientIds.remove(clientId)
        sharedPreferences.edit()
            .putStringSet(PrefKeyClientIds, clientIds)
            .remove(getPublicKeyKey(clientId))
            .remove(getLastRequestIdKey(clientId))
            .apply()
        securedPreferences.edit()
            .remove(getSecretKeyKey(clientId))
            .apply()
    }

    private fun getPublicKeyKey(clientId: String): String {
        return "tonConnectPublicKey_$clientId"
    }

    private fun getSecretKeyKey(clientId: String): String {
        return "tonConnectSecretKey_$clientId"
    }

    private fun getLastRequestIdKey(clientId: String): String {
        return "tonConnectLastRequestId_$clientId"
    }

    private companion object {

        private const val PrefKeyClientIds = "tonConnectClientIds"
    }
}