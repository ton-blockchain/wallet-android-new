package org.ton.wallet.di

sealed interface DiProvider<T> {

    fun interface Factory<T> : DiProvider<T> {

        fun build(): T
    }

    class Singleton<T>(private val provider: () -> T) : DiProvider<T> {

        val instance: T by lazy { provider.invoke() }
    }
}