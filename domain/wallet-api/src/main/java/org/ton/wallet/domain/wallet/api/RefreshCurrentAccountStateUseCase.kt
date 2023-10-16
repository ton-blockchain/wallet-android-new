package org.ton.wallet.domain.wallet.api

interface RefreshCurrentAccountStateUseCase {

    suspend fun invoke()
}