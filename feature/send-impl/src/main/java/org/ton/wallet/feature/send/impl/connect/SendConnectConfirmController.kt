package org.ton.wallet.feature.send.impl.connect

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.core.Res
import org.ton.wallet.core.ThreadUtils
import org.ton.wallet.coreui.ext.setOnClickListenerWithLock
import org.ton.wallet.feature.send.impl.R
import org.ton.wallet.screen.controller.BaseViewModelBottomSheetController
import org.ton.wallet.screen.getScreenArguments
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.uicomponents.BottomSheetHelper
import org.ton.wallet.uicomponents.drawable.EmptyDrawable
import org.ton.wallet.uicomponents.drawable.IndeterminateProgressDrawable
import org.ton.wallet.uicomponents.vh.LineDividerItemDecoration
import org.ton.wallet.uicomponents.view.AppToolbar

class SendConnectConfirmController(args: Bundle?) : BaseViewModelBottomSheetController<SendConnectConfirmViewModel>(args),
    SendConnectConfirmAdapter.Callback {

    override val viewModel by viewModels { SendConnectConfirmViewModel(args.getScreenArguments()) }
    override val isFullHeight: Boolean = true

    private val drawableSize = Res.dp(20)
    private val confirmEmptyDrawable = EmptyDrawable(drawableSize, drawableSize)
    private val loadingDrawable = IndeterminateProgressDrawable(drawableSize)
    private val adapter = SendConnectConfirmAdapter(this)

    private lateinit var rootView: View
    private lateinit var confirmButton: TextView
    private lateinit var buttonsLayout: ViewGroup
    private lateinit var doneImage: ImageView
    private lateinit var bottomShadowView: View
    private lateinit var recyclerView: RecyclerView

    private var prevIsSent = false

    override fun createBottomSheetView(inflater: LayoutInflater, container: ViewGroup?, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_send_confirm_connect, container, false)

        recyclerView = view.findViewById(R.id.sendConfirmRecyclerView)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(LineDividerItemDecoration())
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        val cancelButton = view.findViewById<TextView>(R.id.sendConfirmConnectCancelButton)
        cancelButton.setOnClickListenerWithLock(viewModel::onCancelClicked)
        confirmButton = view.findViewById(R.id.sendConfirmConnectConfirmButton)
        confirmButton.setOnClickListenerWithLock(viewModel::onConfirmClicked)
        confirmButton.setCompoundDrawablesWithIntrinsicBounds(confirmEmptyDrawable, null, confirmEmptyDrawable, null)

        buttonsLayout = view.findViewById(R.id.sendConfirmConnectButtonsLayout)
        doneImage = view.findViewById(R.id.sendConfirmConnectDoneImage)
        bottomShadowView = view.findViewById(R.id.sendConfirmConnectBottomShadow)

        val toolbar = view.findViewById<AppToolbar>(R.id.sendConfirmConnectToolbar)
        BottomSheetHelper.connectAppToolbarWithScrollableView(toolbar, recyclerView)
        rootView = view
        return view
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        viewModel.stateFlow.launchInViewScope(::setState)
    }

    override fun onClose() {
        viewModel.dismissDialog()
        super.onClose()
    }

    override fun onShowDetailsClicked() {
        viewModel.onShowDetailsClicked()
    }

    private fun setState(state: SendConnectConfirmState) {
        adapter.setItems(state.adapterItems)
        ThreadUtils.postOnMain({
            bottomShadowView.isVisible = recyclerView.canScrollVertically(1) || recyclerView.canScrollVertically(-1)
        }, 32L)

        val continueButtonDrawable =
            if (state.dataState == SendConnectConfirmState.DataState.Sending) loadingDrawable
            else confirmEmptyDrawable
        confirmButton.setCompoundDrawablesWithIntrinsicBounds(confirmEmptyDrawable, null, continueButtonDrawable, null)

        val isSent = state.dataState == SendConnectConfirmState.DataState.Sent
        if (prevIsSent != isSent && isSent) {
            animateDone()
        }
        prevIsSent = isSent
    }

    private fun animateDone() {
        buttonsLayout.animate().cancel()
        buttonsLayout.animate()
            .alpha(0f)
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(150L)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    doneImage.alpha = 0f
                    doneImage.scaleX = 0f
                    doneImage.scaleY = 0f
                    doneImage.isVisible = true

                    doneImage.animate().cancel()
                    doneImage.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150L)
                        .start()
                }
            })
            .start()
    }
}