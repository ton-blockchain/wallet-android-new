package org.ton.wallet.app.util

import org.ton.wallet.core.Res
import org.ton.wallet.data.tonclient.api.TonApiException
import org.ton.wallet.strings.RString
import java.net.UnknownHostException

object AppErrorHandler {

    fun getErrorMessage(throwable: Throwable): String? {
        return when (val cause = getException(throwable)) {
            is UnknownHostException -> {
                Res.str(RString.network_error)
            }
            is TonApiException -> {
                if (cause.message?.startsWith("LITE_SERVER_NOTREADY") == true) {
                    null
                } else {
                    Res.str(RString.unknown_error)
                }
            }
            else -> {
                Res.str(RString.unknown_error)
            }
        }
    }

    private fun getException(throwable: Throwable): Throwable? {
        return when (throwable) {
            is UnknownHostException -> throwable
            is TonApiException -> throwable
            else -> throwable.cause?.let(::getException)
        }
    }
}