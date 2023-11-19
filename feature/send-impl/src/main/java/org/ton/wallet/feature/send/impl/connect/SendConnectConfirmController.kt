package org.ton.wallet.feature.send.impl.connect

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.coreui.ext.setOnClickListenerWithLock
import org.ton.wallet.feature.send.impl.R
import org.ton.wallet.rlottie.RLottieDrawable
import org.ton.wallet.rlottie.RLottieResourceLoader
import org.ton.wallet.screen.controller.BaseViewModelBottomSheetController
import org.ton.wallet.screen.getScreenArguments
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.uicomponents.drawable.EmptyDrawable
import org.ton.wallet.uicomponents.drawable.IndeterminateProgressDrawable
import org.ton.wallet.uikit.*

class SendConnectConfirmController(args: Bundle?) : BaseViewModelBottomSheetController<SendConnectConfirmViewModel>(args) {

    override val viewModel by viewModels { SendConnectConfirmViewModel(args.getScreenArguments()) }

    private val drawableSize = Res.dp(20)
    private val emptyDrawable = EmptyDrawable(drawableSize, drawableSize)
    private val loadingDrawable = IndeterminateProgressDrawable(drawableSize)

    private lateinit var rootView: View
    private lateinit var amountText: TextView
    private lateinit var receiverTitleText: TextView
    private lateinit var feeTitleText: TextView
    private lateinit var receiverText: TextView
    private lateinit var feeText: TextView
    private lateinit var cancelButton: TextView
    private lateinit var confirmButton: TextView
    private lateinit var buttonsLayout: ViewGroup
    private lateinit var feeLoadingView: View
    private lateinit var doneImage: ImageView
    private lateinit var senderLayout: ViewGroup
    private lateinit var senderTitleText: TextView
    private lateinit var senderText: TextView
    private lateinit var messageLayout: ViewGroup
    private lateinit var messageTitleText: TextView
    private lateinit var messageText: TextView

    private var prevIsSent = false

    override fun createBottomSheetView(inflater: LayoutInflater, container: ViewGroup?, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_send_confirm_connect, container, false)

        amountText = view.findViewById(R.id.sendConfirmConnectAmountText)
        RLottieResourceLoader.readRawResourceAsync(context, RUiKitRaw.lottie_main) { json, _, _ ->
            val animationDrawable = RLottieDrawable(json, "" + RUiKitRaw.lottie_main, Res.dp(44), Res.dp(44), true)
            animationDrawable.setAutoRepeat(1)
            animationDrawable.start()
            amountText.setCompoundDrawablesRelativeWithIntrinsicBounds(animationDrawable, null, null, null)
        }

        senderLayout = view.findViewById(R.id.sendConfirmConnectSenderLayout)
        senderTitleText = view.findViewById(R.id.sendConfirmConnectSenderTitleText)
        senderText = view.findViewById(R.id.sendConfirmConnectSenderText)

        receiverText = view.findViewById(R.id.sendConfirmConnectReceiverText)
        receiverTitleText = view.findViewById(R.id.sendConfirmConnectReceiverTitleText)

        messageLayout = view.findViewById(R.id.sendConfirmConnectMessageLayout)
        messageTitleText = view.findViewById(R.id.sendConfirmConnectMessageTitleText)
        messageText = view.findViewById(R.id.sendConfirmConnectMessageText)

        feeTitleText = view.findViewById(R.id.sendConfirmConnectFeeTitleText)
        feeText = view.findViewById(R.id.sendConfirmConnectFeeText)

        cancelButton = view.findViewById(R.id.sendConfirmConnectCancelButton)
        cancelButton.setOnClickListenerWithLock(viewModel::onCancelClicked)
        confirmButton = view.findViewById(R.id.sendConfirmConnectConfirmButton)
        confirmButton.setOnClickListenerWithLock(viewModel::onConfirmClicked)
        confirmButton.setCompoundDrawablesWithIntrinsicBounds(emptyDrawable, null, emptyDrawable, null)
        buttonsLayout = view.findViewById(R.id.sendConfirmConnectButtonsLayout)
        doneImage = view.findViewById(R.id.sendConfirmConnectDoneImage)

        feeLoadingView = view.findViewById(R.id.sendConfirmConnectFeeLoaderView)
        val feeDrawable = IndeterminateProgressDrawable(null)
        feeDrawable.setColor(Res.color(RUiKitColor.blue))
        feeLoadingView.background = feeDrawable

        rootView = view
        return view
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        viewModel.stateFlow.launchInViewScope(::setState)
    }

    override fun onAnimationFinished() {
        super.onAnimationFinished()
        rootView.requestLayout()
        senderTitleText.requestLayout()
        receiverTitleText.requestLayout()
        feeTitleText.requestLayout()
        messageTitleText.requestLayout()
    }

    private fun setState(state: SendConnectConfirmState) {
        val amountString = Formatter.getFormattedAmount(state.amount)
        amountText.text = Formatter.getBeautifiedAmount(amountString)
        senderLayout.isVisible = state.senderUfAddress != null
        senderText.text = state.senderUfAddress?.let { address ->
            Formatter.getBeautifiedShortStringSafe(Formatter.getShortAddress(address), Res.font(RUiKitFont.roboto_regular))
        }
        receiverText.text = Formatter.getBeautifiedShortStringSafe(Formatter.getShortAddress(state.receiverUfAddress), Res.font(RUiKitFont.roboto_regular))
        feeLoadingView.isVisible = state.feeString == null
        feeText.text = state.feeString
        val continueButton = if (state.isSending) loadingDrawable else emptyDrawable
        confirmButton.setCompoundDrawablesWithIntrinsicBounds(emptyDrawable, null, continueButton, null)

        messageLayout.isVisible = state.payload != null
        messageText.text = state.payload

        if (prevIsSent != state.isSent && state.isSent) {
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
        prevIsSent = state.isSent
    }
}