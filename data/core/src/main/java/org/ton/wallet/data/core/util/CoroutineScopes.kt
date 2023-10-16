package org.ton.wallet.data.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

object CoroutineScopes {

    val appScope = CoroutineScope(SupervisorJob())
    val repositoriesScope = CoroutineScope(SupervisorJob())
}