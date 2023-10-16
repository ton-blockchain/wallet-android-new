package org.ton.wallet.feature.transactions.impl

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import org.ton.wallet.rlottie.RLottieImageView
import org.ton.wallet.screen.BaseBinding
import org.ton.wallet.uicomponents.view.AppToolbar

internal class TransactionDetailsBinding(inflater: LayoutInflater, container: ViewGroup?) : BaseBinding(R.layout.screen_transaction_details, inflater, container) {

    val toolbar: AppToolbar = root.findViewById(R.id.transactionToolbar)
    val animationView: RLottieImageView = root.findViewById(R.id.transactionAnimationView)
    val amountText: TextView = root.findViewById(R.id.transactionAmountText)
    val feeText: TextView = root.findViewById(R.id.transactionFeeText)
    val dateText: TextView = root.findViewById(R.id.transactionDateText)
    val statusText: TextView = root.findViewById(R.id.transactionStatusText)
    val messageText: TextView = root.findViewById(R.id.transactionMessageText)
    val peerAddressLayout: ViewGroup = root.findViewById(R.id.transactionPeerAddressLayout)
    val peerAddressTitleText: TextView = root.findViewById(R.id.transactionPeerAddressTitleText)
    val peerAddressValueText: TextView = root.findViewById(R.id.transactionPeerAddressValueText)
    val recipientLayout: ViewGroup = root.findViewById(R.id.transactionRecipientLayout)
    val recipientAddressText: TextView = root.findViewById(R.id.transactionRecipientValueText)
    val scrollView: NestedScrollView = root.findViewById(R.id.transactionScrollView)
    val hashLayout: ViewGroup = root.findViewById(R.id.transactionHashLayout)
    val hashValueText: TextView = root.findViewById(R.id.transactionHashValueText)
    val button: TextView = root.findViewById(R.id.transactionButton)
    val explorerButton: TextView = root.findViewById(R.id.transactionExplorerButton)
}