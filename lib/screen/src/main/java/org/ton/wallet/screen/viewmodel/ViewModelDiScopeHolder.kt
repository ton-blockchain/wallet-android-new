package org.ton.wallet.screen.viewmodel

import org.ton.wallet.di.DiScope

object ViewModelDiScopeProvider {

    private lateinit var diScopeProvider: () -> DiScope

    val diScope: DiScope get() = diScopeProvider.invoke()

    fun init(provider: () -> DiScope) {
        diScopeProvider = provider
    }
}