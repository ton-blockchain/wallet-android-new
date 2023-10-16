package org.ton.wallet.domain.wallet.api

import kotlinx.coroutines.flow.Flow

interface GetCurrentAccountBalanceUseCase {

    fun getTonBalanceFlow(): Flow<Long?>

    fun getFiatBalanceStringFlow(): Flow<String>
}