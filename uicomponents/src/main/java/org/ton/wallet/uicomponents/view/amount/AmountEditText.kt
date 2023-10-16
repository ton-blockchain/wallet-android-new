package org.ton.wallet.uicomponents.view.amount

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.text.*
import android.util.*
import android.view.animation.OvershootInterpolator
import androidx.annotation.Px
import androidx.core.math.MathUtils.clamp
import androidx.core.view.updatePadding
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.rlottie.RLottieDrawable
import org.ton.wallet.rlottie.RLottieResourceLoader
import org.ton.wallet.uicomponents.UiConst
import org.ton.wallet.uicomponents.util.TextWatcherAdapter
import org.ton.wallet.uicomponents.view.AppEditText
import org.ton.wallet.uikit.RUiKitRaw
import kotlin.math.*

class AmountEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppEditText(context, attrs) {

    private val textWatcher = object : TextWatcherAdapter {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            super.onTextChanged(s, start, before, count)
            setNewText(s ?: "")
        }
    }

    private val defaultTextPaint: TextPaint? = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val hintTextPaint: TextPaint? = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val smallTextPaint: TextPaint? = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val debugPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val textPropertiesMap = ArrayMap<CharSequence, TextProperties>()
    private val prevLettersPropertiesList = mutableListOf<CharProperties>()
    private val currLettersPropertiesList = mutableListOf<CharProperties>()
    private val decimalSeparator = Formatter.decimalSeparator

    private var hintStaticLayout: StaticLayout? = null
    private var hintAnimationStartTimestamp: Long = 0

    private var gemDrawable: RLottieDrawable? = null
    private var currentTextHeight: Int = 0
    private var initialTextSize: Float = 0f
    private var maxTextWidth: Int = 0
    private var oldText: CharSequence = ""
    private var prevTextSize: Float = 0f
    private var prevSeparatorPosition: Int = 0
    private var currContentWidth: Int = 0
    private var prevContentWidth: Int = 0
    private var prevIsSymbolAdded: Boolean = false

    init {
        debugPaint.color = Color.BLUE
        addTextChangedListener(textWatcher)
        RLottieResourceLoader.readRawResourceAsync(context, RUiKitRaw.lottie_start) { json, _, _ ->
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
        updatePadding(top = textSize.roundToInt(), bottom = textSize.roundToInt())
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val specMode = MeasureSpec.getMode(widthMeasureSpec)
        if (specMode == MeasureSpec.AT_MOST || specMode == MeasureSpec.EXACTLY) {
            maxTextWidth = MeasureSpec.getSize(widthMeasureSpec) - UiConst.MainAnimationSize - DrawablePadding
        }
        if (initialTextSize == 0f) {
            initialTextSize = paint.textSize
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw) {
            textPropertiesMap.clear()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        gemDrawable?.start()
        defaultTextPaint?.typeface = paint.typeface
        hintTextPaint?.typeface = paint.typeface
        smallTextPaint?.typeface = paint.typeface
    }

    override fun onDetachedFromWindow() {
        gemDrawable?.stop()
        super.onDetachedFromWindow()
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return who == gemDrawable || super.verifyDrawable(who)
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        defaultTextPaint?.color = color
        smallTextPaint?.color = color
    }

    override fun setTextSize(size: Float) {
        super.setTextSize(size)
        val defaultSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, size, context.resources.displayMetrics)
        defaultTextPaint?.textSize = defaultSize
        hintTextPaint?.textSize = defaultSize
        smallTextPaint?.textSize = defaultSize * Formatter.BeatifiedAmountSmallFactor
        initHintStaticLayout()
    }

    override fun onDraw(canvas: Canvas) {
        val hintColor = currentHintTextColor
        val textColor = currentTextColor
        setHintTextColor(Color.TRANSPARENT)
        setTextColor(Color.TRANSPARENT)
        super.onDraw(canvas)
        setHintTextColor(hintColor)
        setTextColor(textColor)

        val currentTimestamp = SystemClock.elapsedRealtime()
        var left = paddingLeft.toFloat() - UiConst.MainAnimationSize - DrawablePadding

        // check progress
        var isAnyAnimationInProgress = false
        var animationsProgressSum = 0f
        for (i in 0 until currLettersPropertiesList.size) {
            val progress = currLettersPropertiesList[i].getAnimationProgress(currentTimestamp)
            animationsProgressSum += progress
            if (!isAnyAnimationInProgress) {
                isAnyAnimationInProgress = progress < 1f
            }
        }
        if (isAnyAnimationInProgress) {
            val animationProgress = if (currLettersPropertiesList.size > 0) {
                clamp(animationsProgressSum / currLettersPropertiesList.size, 0f, 1f)
            } else {
                0f
            }
            if (0f < animationProgress && animationProgress < 1f) {
                val progress = if (currContentWidth > prevContentWidth) 1f - animationProgress else animationProgress
                left += abs(currContentWidth - prevContentWidth) * progress * 0.5f
            }
        }

        // draw gem
        gemDrawable?.let { drawable ->
            val drawableTop = (height - drawable.bounds.height()) / 2
            drawable.setBounds(left.roundToInt(), drawableTop, left.roundToInt() + drawable.bounds.width(), drawableTop + drawable.bounds.height())
            drawable.draw(canvas)
            left += DrawablePadding + drawable.bounds.width()
        }

        val firstLetterStaticLayout =
            if (currLettersPropertiesList.isNotEmpty()) currLettersPropertiesList[0].staticLayout
            else null
        val firstLetterDescent = firstLetterStaticLayout?.getLineDescent(0) ?: 0
        val firstLetterHeight = firstLetterStaticLayout?.height ?: 0
        val yBaselineOffset = (measuredHeight - firstLetterHeight) * 0.5f

        // hint
        val text = text?.toString() ?: ""
        val hintProgress =
            if (hintAnimationStartTimestamp != 0L) {
                var progress = (SystemClock.elapsedRealtime() - hintAnimationStartTimestamp).toFloat() / AnimationDurationMs
                if (progress > 1f) {
                    hintAnimationStartTimestamp = 0L
                    progress = 1f
                }
                if (text.isEmpty()) 1f - progress else progress
            } else {
                if (text.isEmpty()) 0f else 1f
            }
        val hintLayout = hintStaticLayout
        if (hintLayout != null) {
            canvas.save()
            val yHint = paddingTop.toFloat() - hintLayout.height * hintProgress
            canvas.translate(left, yHint)
            val scaleProgress = SymbolHiddenScale + (1f - SymbolHiddenScale) * (1f - hintProgress)
            canvas.scale(scaleProgress, scaleProgress, hintLayout.width * 0.5f, hintLayout.height * 0.5f)
            hintLayout.paint.alpha = (Color.alpha(currentHintTextColor) * (1f - hintProgress)).roundToInt()
            hintLayout.draw(canvas)
            canvas.restore()
        }

        // letters
        for (i in 0 until currLettersPropertiesList.size) {
            val staticLayout = currLettersPropertiesList[i].staticLayout
            val layoutBottom = staticLayout.getLineBottom(0)
            val layoutDescent = staticLayout.getLineDescent(0)
            val letterAnimationProgress = currLettersPropertiesList[i].getAnimationProgress(currentTimestamp)
            val yOffset = yBaselineOffset +
                    (firstLetterHeight - staticLayout.height) -
                    (firstLetterDescent - layoutDescent) +
                    staticLayout.height * (1f - SymbolVerticalInterpolator.getInterpolation(letterAnimationProgress))

            canvas.save()
            canvas.translate(left, yOffset)
            val scaleProgress = SymbolHiddenScale + (1f - SymbolHiddenScale) * letterAnimationProgress
            canvas.scale(scaleProgress, scaleProgress, staticLayout.width.toFloat() * 0.5f, (layoutBottom - layoutDescent).toFloat())
            staticLayout.paint.alpha = (255 * letterAnimationProgress).roundToInt()
            staticLayout.draw(canvas)
            canvas.restore()

            left += staticLayout.getLineWidth(0)
        }
    }

    private fun setNewText(newText: CharSequence) {
        if (width <= 0) {
            return
        }

        val currIsSymbolAdded = newText.length > oldText.length
        val newString = newText.toString()
        val currentTimestamp = SystemClock.elapsedRealtime()
        if ((oldText.isEmpty() && newText.isNotEmpty() || oldText.isNotEmpty() && newText.isEmpty())) {
            hintAnimationStartTimestamp =
                if (hintAnimationStartTimestamp == 0L) {
                    currentTimestamp
                } else {
                    currentTimestamp - AnimationDurationMs + (currentTimestamp - hintAnimationStartTimestamp)
                }
        } else if (hintAnimationStartTimestamp == 0L) {
            hintAnimationStartTimestamp = 0
        }

        var textProperties = textPropertiesMap[newText]
        if (textProperties == null) {
            paint.textSize = initialTextSize
            var staticLayout = getTextStaticLayout(newText)
            while (staticLayout.lineCount > 1) {
                paint.textSize -= Res.sp(1f)
                staticLayout = getTextStaticLayout(newText)
            }

            textProperties = TextProperties(size = paint.textSize, height = staticLayout.height)
            textPropertiesMap[newText] = textProperties
        }
        defaultTextPaint?.textSize = textProperties.size
        hintTextPaint?.textSize = textProperties.size
        smallTextPaint?.textSize = textProperties.size * Formatter.BeatifiedAmountSmallFactor
        paint.textSize = textProperties.size
        currentTextHeight = textProperties.height
        initHintStaticLayout()

        val separatorPosition = newString.indexOf(decimalSeparator)
        prevLettersPropertiesList.clear()
        if (prevSeparatorPosition == -1 || separatorPosition != -1) {
            prevLettersPropertiesList.addAll(currLettersPropertiesList)
        }
        prevSeparatorPosition = separatorPosition

        currLettersPropertiesList.clear()
        var totalTextWidth = 0f
        val isTextSizeEquals = prevTextSize == textProperties.size
        val withAnimation = newString.length > oldText.length && newString.startsWith(oldText) && isTextSizeEquals
        for (i in newString.indices) {
            val newChar: String = newString.substring(i, i + 1)
            val oldChar: String? =
                if (prevLettersPropertiesList.isNotEmpty() && i < oldText.length) oldText.substring(i, i + 1)
                else null
            val charProperties = if (oldChar == newChar) {
                if (isTextSizeEquals) {
                    prevLettersPropertiesList[i]
                } else {
                    CharProperties(
                        staticLayout = getCharStaticLayout(newString, i, separatorPosition),
                        animationStartTimestamp = prevLettersPropertiesList[i].animationStartTimestamp
                    )
                }
            } else {
                val staticLayout = getCharStaticLayout(newString, i, separatorPosition)
                CharProperties(staticLayout, if (withAnimation) currentTimestamp else 0)
            }
            currLettersPropertiesList.add(charProperties)
            totalTextWidth += charProperties.staticLayout.getLineWidth(0)
        }

        val hintWidth = hintStaticLayout?.getLineWidth(0) ?: 0f
        if (currLettersPropertiesList.isEmpty()) {
            prevContentWidth = ceil(hintWidth).toInt()
        }

        val textWidth = max(totalTextWidth, hintStaticLayout?.getLineWidth(0) ?: 0f)
        currContentWidth = ceil(textWidth).roundToInt()
        val padding = (width - currContentWidth) / 2 - Res.dp(2)
        val leftPadding = padding + UiConst.MainAnimationSize / 2
        val rightPadding = padding - UiConst.MainAnimationSize / 2
        updatePadding(left = leftPadding, right = rightPadding)

        prevTextSize = textProperties.size
        prevIsSymbolAdded = currIsSymbolAdded
        oldText = newText

        invalidate()
    }

    private fun getCharStaticLayout(text: CharSequence, charPosition: Int, separatorPosition: Int): StaticLayout {
        val paint =
            if (separatorPosition == -1 || charPosition < separatorPosition) defaultTextPaint
            else smallTextPaint
        val charWidth = ceil(paint?.measureText(text, charPosition, charPosition + 1) ?: 0f).toInt()
        return StaticLayout(text, charPosition, charPosition + 1, paint, charWidth, Layout.Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineSpacingExtra, includeFontPadding)
    }

    private fun getTextStaticLayout(text: CharSequence): StaticLayout {
        return StaticLayout(text, paint, maxTextWidth, Layout.Alignment.ALIGN_CENTER, lineSpacingMultiplier, lineSpacingExtra, includeFontPadding)
    }

    private fun initHintStaticLayout() {
        hintTextPaint?.color = currentHintTextColor
        val hintWidth = hintTextPaint?.measureText(hint, 0, hint.length)?.roundToInt() ?: 0
        hintStaticLayout = StaticLayout(hint, hintTextPaint, hintWidth, Layout.Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineSpacingExtra, includeFontPadding)
    }

    private companion object {

        private const val AnimationDurationMs = 200L
        private const val SymbolHiddenScale = 0.9f

        private val DrawablePadding = Res.dp(4)
        private val SymbolVerticalInterpolator = OvershootInterpolator(2.0f)
    }

    private class TextProperties(
        @Px val size: Float,
        @Px val height: Int
    )

    private class CharProperties(
        val staticLayout: StaticLayout,
        val animationStartTimestamp: Long
    ) {

        fun getAnimationProgress(currentTimestamp: Long): Float {
            var progress = (currentTimestamp - abs(animationStartTimestamp)).toFloat() / AnimationDurationMs
            if (animationStartTimestamp < 0) {
                progress = 1f - progress
            }
            return clamp(progress, 0f, 1f)
        }
    }
}