package org.ton.wallet.uicomponents.view.amount

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.text.*
import android.util.AttributeSet
import android.view.View
import androidx.core.math.MathUtils.clamp
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.rlottie.RLottieDrawable
import org.ton.wallet.rlottie.RLottieResourceLoader
import org.ton.wallet.uicomponents.UiConst
import org.ton.wallet.uikit.*
import kotlin.math.*

class AmountView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val decimalSeparator = Formatter.decimalSeparator
    private val defaultTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val smallTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var animationStartTimestamp = 0L
    private var bigSymbolsCount = 0
    private var gemDrawable: RLottieDrawable? = null
    private var maxTextWidth = 0
    private var text: CharSequence = ""
    private var textCharWidths: FloatArray? = null
    private var textCenterY = 0f
    private var textHeight = 0f
    private var textWidth = 0f

    init {
        defaultTextPaint.color = Res.color(RUiKitColor.common_white)
        defaultTextPaint.textSize = InitialTextSize
        defaultTextPaint.typeface = Res.font(RUiKitFont.productsans_medium)
        smallTextPaint.color = defaultTextPaint.color
        smallTextPaint.textSize = defaultTextPaint.textSize * Formatter.BeatifiedAmountSmallFactor
        smallTextPaint.typeface = Res.font(RUiKitFont.productsans_medium)

        RLottieResourceLoader.readRawResourceAsync(context, RUiKitRaw.lottie_main) { json, _, _ ->
            gemDrawable = RLottieDrawable(json, "" + RUiKitRaw.lottie_main, UiConst.MainAnimationSize, UiConst.MainAnimationSize, true)
            gemDrawable!!.setAutoRepeat(1)
            gemDrawable!!.setBounds(0, 0, gemDrawable!!.intrinsicWidth, gemDrawable!!.intrinsicHeight)
            if (isAttachedToWindow) {
                gemDrawable!!.start()
            }
            gemDrawable!!.callback = this
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val nonTextWidth = paddingLeft + UiConst.MainAnimationSize + DrawablePadding + paddingRight
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            maxTextWidth = MeasureSpec.getSize(widthMeasureSpec) - nonTextWidth
        }
        val height = max(UiConst.MainAnimationSize.toFloat(), textHeight)
        val width = nonTextWidth + textWidth
        setMeasuredDimension(ceil(width).toInt(), ceil(height).toInt())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        gemDrawable?.start()
    }

    override fun onDetachedFromWindow() {
        gemDrawable?.stop()
        super.onDetachedFromWindow()
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return who == gemDrawable || super.verifyDrawable(who)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var currentTimestamp = SystemClock.elapsedRealtime()
        val totalAnimationProgress = clamp((currentTimestamp - animationStartTimestamp).toFloat() / TotalAnimationDurationMs, 0f, 1f)

        var left = paddingLeft.toFloat() + textWidth * 0.5f * (1f - totalAnimationProgress)
        gemDrawable?.let { drawable ->
            val drawableTop = (height - drawable.bounds.height()) / 2
            drawable.setBounds(left.roundToInt(), drawableTop, left.roundToInt() + drawable.bounds.width(), drawableTop + drawable.bounds.height())
            drawable.draw(canvas)
            left += drawable.bounds.width() + DrawablePadding
        }

        val smallSymbolAnimationDurationMs = (TotalAnimationDurationMs.toFloat() / ((text.length - bigSymbolsCount) + bigSymbolsCount * BigSymbolAnimationFraction)).roundToInt()
        val bigSymbolAnimationDurationMs = (smallSymbolAnimationDurationMs * BigSymbolAnimationFraction).roundToInt()
        val yText = height * 0.5f - textCenterY
        var currentSymbolAnimationStartTimestamp = animationStartTimestamp
        for (i in text.indices) {
            currentTimestamp = SystemClock.elapsedRealtime()
            val isBeforeOrDecimalSeparator = i <= bigSymbolsCount
            val currentSymbolAnimationDurationMs =
                if (isBeforeOrDecimalSeparator) bigSymbolAnimationDurationMs
                else smallSymbolAnimationDurationMs
            val symbolProgress =
                if (currentTimestamp < currentSymbolAnimationStartTimestamp) 0f
                else clamp((currentTimestamp - currentSymbolAnimationStartTimestamp).toFloat() / currentSymbolAnimationDurationMs, 0f, 1f)

            val textPaint =
                if (isBeforeOrDecimalSeparator) defaultTextPaint
                else smallTextPaint
            textPaint.alpha = (symbolProgress * 255f).roundToInt()

            val isNeedScale = totalAnimationProgress < 1f && isBeforeOrDecimalSeparator
            if (isNeedScale) {
                canvas.save()
                canvas.scale(symbolProgress, symbolProgress, left + textCharWidths!![i], yText)
            }

            canvas.drawText(text, i, i + 1, left, yText, textPaint)

            if (isNeedScale) {
                canvas.restore()
            }

            left += textCharWidths!![i]
            currentSymbolAnimationStartTimestamp += currentSymbolAnimationDurationMs
        }
    }

    fun setText(text: CharSequence?, isAnimated: Boolean) {
        if (this.text == text) {
            return
        }
        this.text = text ?: ""
        textWidth = 0f
        if (text.isNullOrEmpty()) {
            textCharWidths = null
        } else {
            // detect text size
            defaultTextPaint.textSize = InitialTextSize
            val formattedText = Formatter.getBeautifiedAmount(text) ?: ""
            var staticLayout = getFormattedTextStaticLayout(formattedText, defaultTextPaint)
            while (staticLayout.lineCount > 1) {
                defaultTextPaint.textSize -= Res.sp(1f)
                staticLayout = getFormattedTextStaticLayout(formattedText, defaultTextPaint)
            }
            smallTextPaint.textSize = defaultTextPaint.textSize * Formatter.BeatifiedAmountSmallFactor
            textHeight = staticLayout.height.toFloat()

            // fill text values
            textCharWidths = FloatArray(text.length)
            var beforeDecimalSeparator = true
            for (i in text.indices) {
                val textPaint = if (beforeDecimalSeparator) defaultTextPaint else smallTextPaint
                val charWidth = textPaint.measureText(text, i, i + 1)
                textCharWidths!![i] = charWidth
                textWidth += charWidth
                if (beforeDecimalSeparator && text[i] == decimalSeparator) {
                    beforeDecimalSeparator = false
                    bigSymbolsCount = i
                }
            }

            val bounds = Rect()
            defaultTextPaint.getTextBounds(text.toString(), 0, text.length, bounds)
            textCenterY = bounds.exactCenterY()
        }

        if (isAnimated) {
            animationStartTimestamp = SystemClock.elapsedRealtime()
        }

        requestLayout()
    }

    private fun getFormattedTextStaticLayout(text: CharSequence, paint: TextPaint): StaticLayout {
        val measuredTextWidth = ceil(paint.measureText(text, 0, text.length)).toInt()
        val textWidth =
            if (maxTextWidth == 0) measuredTextWidth
            else min(measuredTextWidth, maxTextWidth)
        return StaticLayout(text, paint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)
    }

    private companion object {

        private const val TotalAnimationDurationMs = 350L
        private const val BigSymbolAnimationFraction = 3f

        private val DrawablePadding = Res.dp(4)
        private val InitialTextSize = Res.dimen(RUiKitDimen.amount_big_text_size)
    }
}