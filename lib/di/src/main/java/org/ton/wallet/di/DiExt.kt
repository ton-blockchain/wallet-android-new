package org.ton.wallet.di

inline fun <reified T : Any> injectScope(scope: DiScope, name: String? = null): Lazy<T> {
    return InjectLazy { scope.getInstance(name) }
}

class InjectLazy<T>(
    private val provider: () -> T
) : Lazy<T> {

    private var _value: T? = null

    override val value: T
        get() {
            if (_value == null) {
                _value = provider.invoke()
            }
            return _value!!
        }

    override fun isInitialized(): Boolean {
        return _value != null
    }
}