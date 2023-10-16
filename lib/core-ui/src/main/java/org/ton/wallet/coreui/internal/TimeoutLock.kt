package org.ton.wallet.coreui.internal

import android.os.Handler
import android.os.Looper

class TimeoutLock(
    private val defaultLockTimeMs: Long
) {

    private val handler = Handler(Looper.getMainLooper())

    @get:Synchronized
    val isLocked: Boolean
        get() = handler.hasMessages(MSG_LOCK)

    @Synchronized
    fun checkAndMaybeLock(): Boolean {
        return checkAndMaybeLock(defaultLockTimeMs)
    }

    @Synchronized
    fun checkAndMaybeLock(lockTimeMs: Long): Boolean {
        if (isLocked) {
            return true
        }
        lock(if (lockTimeMs == 0L) defaultLockTimeMs else lockTimeMs)
        return false
    }

    @Synchronized
    fun lock() {
        lock(defaultLockTimeMs)
    }

    @Synchronized
    fun lock(lockTimeMs: Long) {
        handler.sendEmptyMessageDelayed(MSG_LOCK, lockTimeMs)
    }

    @Synchronized
    fun unlock() {
        handler.removeMessages(MSG_LOCK)
    }

    private companion object {

        private const val MSG_LOCK = 0
    }
}