package org.ton.wallet.feature.wallet.impl.main

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.feature.wallet.impl.R
import org.ton.wallet.rlottie.RLottieImageView
import org.ton.wallet.screen.BaseBinding
import org.ton.wallet.uicomponents.view.PullToRefreshLayout

internal class MainScreenControllerBinding(container: ViewGroup): BaseBinding(R.layout.screen_main, container) {

    val pullToRefreshLayout: PullToRefreshLayout = root.findViewById(R.id.mainPullToRefreshLayout)
    val recyclerView: RecyclerView = root.findViewById(R.id.mainRecyclerView)
    val scanButton: View = root.findViewById(R.id.mainScanButton)
    val settingsButton: View = root.findViewById(R.id.mainSettingsButton)
    val toolbarLayout: ViewGroup = root.findViewById(R.id.toolbarLayout)
    val toolbarBalanceText: TextView = root.findViewById(R.id.mainToolbarBalanceText)
    val toolbarFiatBalanceText: TextView = root.findViewById(R.id.mainToolbarFiatBalanceText)
    val toolbarAnimationView: RLottieImageView = root.findViewById(R.id.mainToolbarAnimationView)
    val toolbarStatusText: TextView = root.findViewById(R.id.mainToolbarStatusText)
}