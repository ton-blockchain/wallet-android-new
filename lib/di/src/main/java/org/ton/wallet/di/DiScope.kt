package org.ton.wallet.di

class DiScope(
    val parentScopes: List<DiScope> = emptyList(),
    builder: DiScope.() -> Unit
) {

    val scopeStorage = DiProviderStorage()

    init {
        builder.invoke(this)
    }

    inline fun <reified T : Any> factory(factory: DiProvider.Factory<T>) {
        factory(null, factory)
    }

    inline fun <reified T : Any> factory(name: String?, factory: DiProvider.Factory<T>) {
        scopeStorage.addProvider(factory, name)
    }

    inline fun <reified T : Any> singleton(noinline provider: () -> T) {
        singleton(null, provider)
    }

    inline fun <reified T : Any> singleton(name: String?, noinline provider: () -> T) {
        scopeStorage.addProvider(DiProvider.Singleton(provider), name)
    }

    inline fun <reified T : Any> getInstance(name: String? = null): T {
        var instance: T? = scopeStorage.getInstance(name)
        if (instance == null) {
            for (parentScope in parentScopes) {
                instance = parentScope.scopeStorage.getInstance(name)
            }
        }
        return instance ?: throw IllegalStateException("No provider for ${T::class}, name: $name")
    }
}