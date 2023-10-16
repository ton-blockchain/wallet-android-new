package org.ton.wallet.domain.tonconnect.api

import kotlinx.coroutines.flow.Flow
import org.ton.wallet.data.tonconnect.api.model.TonConnectEvent

interface TonConnectGetEventsUseCase {

    fun invoke(): Flow<TonConnectEvent>
}