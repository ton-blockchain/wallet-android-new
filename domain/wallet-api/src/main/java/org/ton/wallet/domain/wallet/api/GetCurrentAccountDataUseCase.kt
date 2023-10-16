package org.ton.wallet.domain.wallet.api

import kotlinx.coroutines.flow.Flow
import org.ton.wallet.data.wallet.api.model.AccountDto

interface GetCurrentAccountDataUseCase {

    suspend fun getAccountState(): AccountDto?

    fun getAccountStateFlow(): Flow<AccountDto>

    fun getAddressFlow(): Flow<String>

    fun getAddressNullableFlow(): Flow<String?>

    fun getIdFlow(): Flow<Int>
}