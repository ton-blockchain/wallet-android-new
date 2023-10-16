package org.ton.wallet.data.core.util

enum class LoadType {
    CacheOrApi,
    OnlyCache,
    OnlyApi;

    val useCache: Boolean
        get() = this == CacheOrApi || this == OnlyCache
}