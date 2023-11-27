package org.ton.wallet.feature.wallet.impl.main

import android.content.Context
import android.widget.FrameLayout
import androidx.annotation.WorkerThread
import org.ton.wallet.feature.wallet.impl.main.adapter.MainTransactionsAdapter
import org.ton.wallet.lib.lists.NoLimitRecycledViewPool
import org.ton.wallet.lib.log.L
import java.util.concurrent.atomic.AtomicBoolean

object MainScreenAdapterHolder {

    const val ViewTypeTransactionItem = 0
    const val ViewTypeTransactionHeader = 1
    const val ViewTypeMainHeader = 2
    const val ViewTypeMainEmpty = 3

    val viewPool = NoLimitRecycledViewPool()

    private val isInitialized = AtomicBoolean(false)

    @WorkerThread
    fun init(context: Context) {
        if (isInitialized.getAndSet(true)) {
            return
        }
        try {
            val stubViewGroup = FrameLayout(context)
            val transactionsAdapter = MainTransactionsAdapter(null)
            for (i in 0 until 15) {
                viewPool.putRecycledView(transactionsAdapter.createViewHolder(stubViewGroup, ViewTypeTransactionItem))
            }
            for (i in 0 until 2) {
                viewPool.putRecycledView(transactionsAdapter.createViewHolder(stubViewGroup, ViewTypeTransactionHeader))
            }
        } catch (e: Throwable) {
            L.e(e)
        }
    }
}