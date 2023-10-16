package org.ton.wallet.domain.tonconnect.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import org.ton.wallet.data.tonconnect.api.TonConnectRepository
import org.ton.wallet.data.tonconnect.api.model.TonConnectEvent
import org.ton.wallet.domain.tonconnect.api.TonConnectGetEventsUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase

class TonConnectGetEventsUseCaseImpl(
    private val tonConnectRepository: TonConnectRepository,
    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase
) : TonConnectGetEventsUseCase {

    override fun invoke(): Flow<TonConnectEvent> {
        return getCurrentAccountDataUseCase.getIdFlow()
            .flatMapLatest(tonConnectRepository::getEventsFlow)
    }
}