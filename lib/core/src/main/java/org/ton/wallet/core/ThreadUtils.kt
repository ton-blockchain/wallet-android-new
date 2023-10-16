package org.ton.wallet.core

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*

object ThreadUtils {

    private val mainHandler = Handler(Looper.getMainLooper())

    private val defaultExecutor = Dispatchers.Default.asExecutor()

    val ioExecutor = Dispatchers.IO.asExecutor()

    val appCoroutineScope = CoroutineScope(Dispatchers.Default)

    fun postOnMain(runnable: Runnable) {
        postOnMain(runnable, 0L)
    }

    fun postOnMain(runnable: Runnable, delayMs: Long) {
        mainHandler.postDelayed(runnable, delayMs)
    }

    fun postOnDefault(runnable: Runnable) {
        defaultExecutor.execute(runnable)
    }

    fun cancelOnMain(runnable: Runnable) {
        mainHandler.removeCallbacks(runnable)
    }
}