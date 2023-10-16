package org.ton.wallet.core.util

class ThreadLocalSafe<T>(
    private val initializer: () -> T
) : ThreadLocal<T>() {

    override fun initialValue(): T? {
        return initializer.invoke()
    }

    fun getSafe(): T {
        return get() ?: initializer.invoke()
    }
}