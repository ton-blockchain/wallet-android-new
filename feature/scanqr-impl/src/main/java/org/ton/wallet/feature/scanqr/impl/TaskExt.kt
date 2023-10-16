package org.ton.wallet.feature.scanqr.impl

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        this.addOnCanceledListener(continuation::cancel)
        this.addOnFailureListener(continuation::resumeWithException)
        this.addOnSuccessListener(continuation::resume)
    }
}