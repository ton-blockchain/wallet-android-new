package org.ton.wallet.data.tonclient.api

import drinkless.org.ton.TonApi

class TonApiException(val error: TonApi.Error) : RuntimeException(error.message)