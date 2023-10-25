package org.ton.wallet.feature.tonconnect.impl

import android.animation.*
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.math.MathUtils.clamp
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import coil.load
import coil.transform.RoundedCornersTransformation
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.coreui.ext.setOnClickListenerWithLock
import org.ton.wallet.coreui.util.*
import org.ton.wallet.screen.controller.BaseViewModelBottomSheetController
import org.ton.wallet.screen.getScreenArguments
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.drawable.EmptyDrawable
import org.ton.wallet.uicomponents.drawable.IndeterminateProgressDrawable
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitFont

class TonConnectApproveController(args: Bundle?) : BaseViewModelBottomSheetController<TonConnectApproveViewModel>(args) {

    override val viewModel by viewModels { TonConnectApproveViewModel(args.getScreenArguments()) }

    private val drawableSize = Res.dp(20)
    private val emptyDrawable = EmptyDrawable(drawableSize, drawableSize)
    private val loadingDrawable = IndeterminateProgressDrawable(drawableSize)

    private lateinit var rootView: View
    private lateinit var loadingView: View
    private lateinit var contentLayout: ViewGroup
    private lateinit var imageView: ImageView
    private lateinit var titleText: TextView
    private lateinit var subTitleText: TextView
    private lateinit var connectButton: TextView
    private lateinit var doneImage: ImageView

    private var prevDataLoading = true

    override fun createBottomSheetView(inflater: LayoutInflater, container: ViewGroup?, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_ton_connect_approve, container, false)
        view.findViewById<View>(R.id.tonConnectApproveCloseButton).setOnClickListenerWithLock(viewModel::onCloseClicked)

        loadingView = view.findViewById(R.id.tonConnectApproveLoadingView)
        val progressDrawable = IndeterminateProgressDrawable()
        progressDrawable.setColor(Res.color(RUiKitColor.blue))
        progressDrawable.setStrokeWidth(Res.dp(4f))
        loadingView.background = progressDrawable

        contentLayout = view.findViewById(R.id.tonConnectApproveContentLayout)

        imageView = view.findViewById(R.id.tonConnectApproveImageView)
        imageView.clipToOutline = true
        imageView.outlineProvider = RoundRectOutlineProvider(Res.dp(24f))

        titleText = view.findViewById(R.id.tonConnectApproveTitleTextView)
        subTitleText = view.findViewById(R.id.tonConnectApproveSubTitleTextView)

        connectButton = view.findViewById(R.id.tonConnectApproveConnectButton)
        connectButton.setCompoundDrawablesWithIntrinsicBounds(emptyDrawable, null, emptyDrawable, null)
        connectButton.setOnClickListenerWithLock(viewModel::onConnectClicked)
        doneImage = view.findViewById(R.id.tonConnectApproveDoneImage)

        rootView = view
        return view
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        viewModel.stateFlow.launchInViewScope(::onStateChanged)
    }

    private fun onStateChanged(state: TonConnectApproveState) {
        titleText.text = Res.str(RString.ton_connect_to, state.appName)

        // image
        if (state.appIconUrl.isNotEmpty()) {
            imageView.load(state.appIconUrl) {
                lifecycle(lifecycleOwner)
                transformations(RoundedCornersTransformation(Res.dp(20f)))
            }
        }
        imageView.isVisible = state.appIconUrl.isNotEmpty()

        // subtitle
        if (state.accountAddress.isNotEmpty()) {
            val shortAddress = Formatter.getShortAddress(state.accountAddress)
            val subtitleBuilder = SpannableStringBuilder(Res.str(RString.ton_connect_requesting_access, state.appHost, shortAddress, state.accountVersion))
            val colorSpanStart = subtitleBuilder.indexOf(shortAddress)
            val colorSpanEnd = colorSpanStart + shortAddress.length
            subtitleBuilder.setSpan(ForegroundColorSpan(Res.color(RUiKitColor.text_secondary)), colorSpanStart, colorSpanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            subtitleBuilder.setSpan(FontSpan(Res.font(RUiKitFont.robotomono_regular)), colorSpanStart, colorSpanStart + 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            subtitleBuilder.setSpan(FontSpan(Res.font(RUiKitFont.robotomono_regular)), colorSpanEnd - 4, colorSpanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            subTitleText.text = subtitleBuilder
        }

        // button
        when (state.connectionState) {
            TonConnectApproveState.ConnectionDefault -> {
                connectButton.setCompoundDrawablesWithIntrinsicBounds(emptyDrawable, null, emptyDrawable, null)
            }
            TonConnectApproveState.ConnectionInProgress -> {
                connectButton.setCompoundDrawablesWithIntrinsicBounds(emptyDrawable, null, loadingDrawable, null)
            }
            TonConnectApproveState.ConnectionConnected -> {
                connectButton.animate().cancel()
                connectButton.animate()
                    .alpha(0f)
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(150L)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            doneImage.isVisible = true
                            doneImage.alpha = 0f
                            doneImage.scaleX = 0f
                            doneImage.scaleY = 0f
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

        if (!state.isDataLoading && prevDataLoading) {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(rootView.width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            contentLayout.measure(widthSpec, heightSpec)
            if (rootView.height < contentLayout.measuredHeight && !isShowHideAnimatorInProgress) {
                val currentTranslation = contentLayout.measuredHeight - rootView.height.toFloat()
                ValueAnimator.ofFloat(0f, 1f).apply {
                    addUpdateListener { animator ->
                        val progress = animator.animatedValue as Float
                        val loadingProgress = 1f - clamp(progress * 2f, 0f, 1f)
                        val contentProgress = clamp((progress - 0.5f) * 2f,0f, 1f)
                        setBottomSheetTranslation(currentTranslation * (1f - progress))
                        loadingView.alpha = loadingProgress
                        loadingView.scaleX = loadingProgress
                        loadingView.scaleY = loadingProgress
                        contentLayout.alpha = contentProgress
                        contentLayout.scaleX = contentProgress
                        contentLayout.scaleY = contentProgress
                    }
                    duration = 200L
                    interpolator = CubicBezierInterpolator.Default
                    start()
                }
            } else {
                loadingView.isVisible = false
            }
        }
        contentLayout.isInvisible = state.isDataLoading
        prevDataLoading = state.isDataLoading
    }
}