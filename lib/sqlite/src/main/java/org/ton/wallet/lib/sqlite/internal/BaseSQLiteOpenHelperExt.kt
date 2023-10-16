package org.ton.wallet.lib.sqlite.internal

import kotlinx.coroutines.*
import org.ton.wallet.lib.sqlite.SQLiteDatabaseWrapper
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

private val suspendingTransactionId = ThreadLocal<Int>()

internal suspend fun <R> BaseSQLiteOpenHelper.withTransaction(block: suspend SQLiteDatabaseWrapper.() -> R): R {
    val transactionContext = coroutineContext[TransactionElement]?.transactionDispatcher
        ?: createTransactionCoroutineContext()
    return withContext(transactionContext) {
        val transactionElement = coroutineContext[TransactionElement]!!
        transactionElement.acquire()
        try {
            writeDatabase.beginTransaction()
            try {
                val result = coroutineScope { block.invoke(writeDatabase) }
                writeDatabase.setTransactionSuccessful()
                result
            } finally {
                writeDatabase.endTransaction()
            }
        } finally {
            transactionElement.release()
        }
    }
}

private suspend fun BaseSQLiteOpenHelper.createTransactionCoroutineContext(): CoroutineContext {
    val controlJob = Job()
    val dispatcher = executor.acquireTransactionThread(controlJob)
    val transactionElement = TransactionElement(controlJob, dispatcher)
    val threadLocalElement = suspendingTransactionId.asContextElement(controlJob.hashCode())
    return dispatcher + transactionElement + threadLocalElement
}
