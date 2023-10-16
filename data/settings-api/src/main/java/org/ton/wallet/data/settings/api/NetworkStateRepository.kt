package org.ton.wallet.data.settings.api

import kotlinx.coroutines.flow.Flow
import org.ton.wallet.data.core.BaseRepository

interface NetworkStateRepository : BaseRepository {

    val networkStateFlow: Flow<NetworkState>
}