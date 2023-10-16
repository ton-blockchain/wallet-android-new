package org.ton.wallet.core.ext

import org.ton.wallet.core.util.ThreadLocalSafe
import java.lang.ref.WeakReference

fun <T> threadLocal(initializer: () -> T): ThreadLocal<T> {
    return object : ThreadLocal<T>() {
        override fun initialValue(): T {
            return initializer.invoke()
        }
    }
}

fun <T> threadLocalSafe(initializer: () -> T): ThreadLocalSafe<T> {
    return ThreadLocalSafe(initializer)
}

fun <T> weak(obj: T): WeakReference<T> {
    return WeakReference(obj)
}