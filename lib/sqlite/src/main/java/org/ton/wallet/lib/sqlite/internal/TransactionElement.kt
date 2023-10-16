package org.ton.wallet.lib.sqlite.internal

import kotlinx.coroutines.Job
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

internal class TransactionElement(
    private val transactionControlJob: Job,
    internal val transactionDispatcher: ContinuationInterceptor
) : CoroutineContext.Element {

    companion object Key : CoroutineContext.Key<TransactionElement>

    override val key: CoroutineContext.Key<*>
        get() = TransactionElement

    private val refCount = AtomicInteger(0)

    fun acquire() {
        refCount.incrementAndGet()
    }

    fun release() {
        val count = refCount.decrementAndGet()
        if (count < 0) {
            throw IllegalStateException("Transaction was never started or was already released")
        } else if (count == 0) {
            transactionControlJob.cancel()
        }
    }
}