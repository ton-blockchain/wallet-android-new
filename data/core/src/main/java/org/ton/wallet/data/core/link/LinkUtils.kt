package org.ton.wallet.data.core.link

import android.net.Uri
import org.json.JSONObject
import org.ton.wallet.core.ext.getStringOrNull
import org.ton.wallet.core.ext.toUriSafe
import org.ton.wallet.data.core.connect.TonConnectRequest

object LinkUtils {

    private const val DeepLinkScheme = "ton"
    private const val DeepLinkTransferAuthority = "transfer"

    private const val ParameterAmount = "amount"
    private const val ParameterText = "text"

    fun getTransferLink(address: String, amount: Long? = null, comment: String? = null): String {
        val builder = Uri.Builder()
            .scheme(DeepLinkScheme)
            .authority(DeepLinkTransferAuthority)
            .path(address)
        if (amount != null) {
            builder.appendQueryParameter("amount", amount.toString())
        }
        if (comment != null) {
            builder.appendQueryParameter("text", comment)
        }
        return builder.build().toString()
    }

    fun parseLink(url: String): LinkAction? {
        val uri = url.toUriSafe() ?: return null
        var action: LinkAction? = parseTransfer(uri)
        if (action == null) {
            action = parseTonConnect(uri)
        }
        return action
    }

    private fun parseTransfer(uri: Uri): LinkAction.TransferAction? {
        if (uri.scheme != DeepLinkScheme || uri.authority != DeepLinkTransferAuthority) {
            return null
        }
        return LinkAction.TransferAction(
            address = uri.path?.removePrefix("/"),
            amount = uri.getQueryParameter(ParameterAmount)?.toLongOrNull(),
            message = uri.getQueryParameter(ParameterText)
        )
    }

    private fun parseTonConnect(uri: Uri): LinkAction.TonConnectAction? {
        val isValidUri = uri.scheme == "tc" ||
                (uri.scheme == "https" && uri.authority == "app.tonkeeper.com" && uri.path == "/ton-connect")
        if (!isValidUri) {
            return null
        }

        val version = uri.getQueryParameter("v")?.toIntOrNull() ?: return null
        val clientId = uri.getQueryParameter("id") ?: return null
        val requestJson = uri.getQueryParameter("r") ?: return null
        val ret = uri.getQueryParameter("ret")

        // request
        val jsonObject = JSONObject(requestJson)
        val manifestUrl = jsonObject.getStringOrNull("manifestUrl") ?: return null
        val jsonArray = jsonObject.optJSONArray("items") ?: return null
        val tonConnectItems = mutableListOf<TonConnectRequest.ConnectItem>()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val name = item.getStringOrNull("name") ?: continue
            when (name) {
                "ton_addr" -> {
                    tonConnectItems.add(TonConnectRequest.ConnectItem.Address)
                }
                "ton_proof" -> {
                    val payload = item.getStringOrNull("payload")
                    tonConnectItems.add(TonConnectRequest.ConnectItem.Proof(payload))
                }
                else -> {
                    tonConnectItems.add(TonConnectRequest.ConnectItem.UnknownMethod(name))
                }
            }
        }
        val request = TonConnectRequest(manifestUrl, tonConnectItems)
        return LinkAction.TonConnectAction(uri.toString(), version, clientId, request)
    }
}