package org.ton.wallet.di

import kotlin.reflect.KClass

class DiProviderStorage {

    val providers = hashMapOf<KClass<*>, HashMap<String?, DiProvider<*>>>()

    inline fun <reified T : Any> addProvider(provider: DiProvider<T>, name: String? = null) {
        val providersMap = providers.getOrPut(T::class) { hashMapOf() }
        if (!providersMap.containsKey(name)) {
            providersMap[name] = provider
        }
    }

    inline fun <reified T : Any> getInstance(name: String? = null): T? {
        val providersMap = providers[T::class] ?: return null
        val provider = providersMap[name] ?: return null
        return when (provider) {
            is DiProvider.Singleton<*> -> provider.instance as T
            is DiProvider.Factory<*> -> provider.build() as T
        }
    }
}