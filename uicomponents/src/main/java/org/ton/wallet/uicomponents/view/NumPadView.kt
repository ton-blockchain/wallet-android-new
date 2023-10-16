package org.ton.wallet.uicomponents.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.*
import androidx.core.view.forEachIndexed
import androidx.core.view.isInvisible
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.uikit.*
import kotlin.math.roundToInt

class NumPadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr),
    View.OnClickListener,
    View.OnLongClickListener {

    private var listener: NumPadViewListener? = null

    init {
        var isDarkTheme = false
        var withDot = false
        val typedArray = context.obtainStyledAttributes(attrs, RUiKitStyleable.NumPadView)
        try {
            isDarkTheme = typedArray.getBoolean(RUiKitStyleable.NumPadView_numpad_dark, isDarkTheme)
            withDot = typedArray.getBoolean(RUiKitStyleable.NumPadView_numpad_dot, withDot)
        } finally {
            typedArray.recycle()
        }

        for (i in 0 until 12) {
            val button = ButtonView(context)
            if (i < 9) {
                button.setNumber(i + 1)
            } else if (i == Position0) {
                button.setNumber(0)
            } else if (i == PositionDot) {
                button.setNumber(NumberDot, Formatter.decimalSeparator.toString(), true)
                button.isInvisible = !withDot
            } else if (i == PositionBackSpace) {
                button.setNumber(NumberBackSpace, null)
            }
            button.setOnClickListener(this)
            addView(button)
        }
        (getChildAt(Position2) as ButtonView).setText("ABC")
        (getChildAt(Position3) as ButtonView).setText("DEF")
        (getChildAt(Position4) as ButtonView).setText("GHI")
        (getChildAt(Position5) as ButtonView).setText("JKL")
        (getChildAt(Position6) as ButtonView).setText("MNO")
        (getChildAt(Position7) as ButtonView).setText("PQRS")
        (getChildAt(Position8) as ButtonView).setText("TUV")
        (getChildAt(Position9) as ButtonView).setText("WXYZ")
        (getChildAt(Position0) as ButtonView).setText("+")
        (getChildAt(PositionBackSpace) as ButtonView).setOnLongClickListener(this)

        setDark(isDarkTheme)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        var buttonWidthSpecMode = MeasureSpec.AT_MOST
        if (widthSpecMode == MeasureSpec.EXACTLY) {
            buttonWidthSpecMode = MeasureSpec.EXACTLY
        }
        var buttonHeightSpecMode = MeasureSpec.AT_MOST
        if (heightSpecMode == MeasureSpec.EXACTLY) {
            buttonHeightSpecMode = MeasureSpec.EXACTLY
        }

        val buttonWidthSpec = MeasureSpec.makeMeasureSpec((widthSpecSize - InnerSpace * (Cols - 1) - paddingLeft - paddingRight) / Cols, buttonWidthSpecMode)
        val buttonHeightSpec = MeasureSpec.makeMeasureSpec((heightSpecSize - InnerSpace * (Rows - 1) - paddingTop - paddingBottom) / Rows, buttonHeightSpecMode)
        for (i in 0 until childCount) {
            getChildAt(i).measure(buttonWidthSpec, buttonHeightSpec)
        }
        val buttonWidth = getChildAt(0).measuredWidth
        val buttonHeight = getChildAt(0).measuredHeight

        val width = buttonWidth * Cols + InnerSpace * (Cols - 1) + paddingLeft + paddingRight
        val height = buttonHeight * Rows + InnerSpace * (Rows - 1) + paddingTop + paddingBottom
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val row = i / Cols
            val col = i % Cols
            val top = row * (child.measuredHeight + InnerSpace) + paddingTop
            val left = col * (child.measuredWidth + InnerSpace) + paddingLeft
            child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
        }
    }

    override fun onClick(v: View?) {
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        val listener = this.listener ?: return
        if (v is ButtonView) {
            val number = v.number
            if (number == NumberBackSpace) {
                listener.onBackSpaceClicked()
            } else if (number == NumberDot) {
                listener.onDotClicked()
            } else if (number != null) {
                listener.onNumberClicked(number)
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        val listener = this.listener ?: return false
        if (v is ButtonView && indexOfChild(v) == PositionBackSpace) {
            listener.onBackSpaceLongClicked()
        }
        return true
    }

    fun setNumPadViewListener(listener: NumPadViewListener) {
        this.listener = listener
    }

    fun setDark(isDark: Boolean) {
        val drawableColor =
            if (isDark) Res.color(RUiKitColor.numpad_button_image_tint_dark)
            else Res.color(RUiKitColor.numpad_button_image_tint_light)
        forEachIndexed { index, view ->
            (view as? ButtonView)?.let { v ->
                v.setDark(isDark)
                if (index == PositionBackSpace) {
                    v.setDrawable(Res.drawableColored(RUiKitDrawable.ic_delete, drawableColor))
                }
            }
        }
    }


    private companion object {

        private val InnerSpace = Res.dp(6)
        private const val Cols = 3
        private const val Rows = 4

        private const val NumberBackSpace = -1
        private const val NumberDot = -2

        private const val Position1 = 0
        private const val Position2 = 1
        private const val Position3 = 2
        private const val Position4 = 3
        private const val Position5 = 4
        private const val Position6 = 5
        private const val Position7 = 6
        private const val Position8 = 7
        private const val Position9 = 8
        private const val PositionDot = 9
        private const val Position0 = 10
        private const val PositionBackSpace = 11
    }

    interface NumPadViewListener {

        fun onNumberClicked(number: Int)

        fun onBackSpaceClicked()

        fun onBackSpaceLongClicked()

        fun onDotClicked() = Unit
    }


    private class ButtonView(context: Context) : View(context) {

        private val numberPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

        var number: Int? = null
            private set
        private var numberString: String? = null
        private var textString: String? = null
        private var drawable: Drawable? = null
        private var isNumberCentered: Boolean = false

        private var xNumber: Float = 0f
        private var yNumber: Float = 0f
        private var xText: Float = 0f
        private var yText: Float = 0f

        init {
            numberPaint.textSize = Res.sp(24f)
            numberPaint.typeface = Res.font(R.font.roboto_regular)
            textPaint.color = Res.color(R.color.numpad_button_text)
            textPaint.textSize = Res.sp(14f)
            textPaint.typeface = Res.font(R.font.roboto_regular)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
            val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
            var width = MeasureSpec.getSize(widthMeasureSpec)
            var height = MeasureSpec.getSize(heightMeasureSpec)
            if (widthSpecMode == MeasureSpec.UNSPECIFIED || widthSpecMode == MeasureSpec.AT_MOST && width > MinWidth) {
                width = MinWidth
            }
            if (heightSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.AT_MOST && height > MinHeight) {
                height = MinHeight
            }
            setMeasuredDimension(width, height)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            if (w != oldw || h != oldh) {
                updateMeasurements()
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            numberString?.let { str ->
                canvas.drawText(str, xNumber, yNumber, numberPaint)
            }
            textString?.let { str ->
                canvas.drawText(str, xText, yText, textPaint)
            }
            drawable?.draw(canvas)
        }

        fun setNumber(number: Int?, string: String? = number?.toString() ?: "", isCentered: Boolean = false) {
            this.number = number
            numberString = string
            isNumberCentered = isCentered
            updateMeasurements()
        }

        fun setText(text: String?) {
            textString = text
            updateMeasurements()
        }

        fun setDrawable(drawable: Drawable?) {
            this.drawable = drawable
            updateMeasurements()
        }

        fun setDark(isDark: Boolean) {
            val backgroundColor =
                if (isDark) Res.color(R.color.numpad_button_background_dark)
                else Res.color(R.color.numpad_button_background_light)
            background = Res.drawableColored(R.drawable.bkg_rect_rounded_6dp, backgroundColor)
            foreground =
                if (isDark) Res.drawable(R.drawable.ripple_rect_6dp_light)
                else Res.drawable(R.drawable.ripple_rect_6dp_dark)
            numberPaint.color =
                if (isDark) Res.color(R.color.common_white)
                else Res.color(R.color.text_primary)
            invalidate()
        }

        private fun updateMeasurements() {
            if (width == 0 || height == 0) {
                return
            }

            numberString?.let { string ->
                val numberStringWidth = numberPaint.measureText(string)
                xNumber =
                    if (isNumberCentered) (width - numberStringWidth) * 0.5f
                    else Res.dp(3) + (width * 0.44f - numberStringWidth) * 0.5f
                yNumber = (height - numberPaint.descent() - numberPaint.ascent()) / 2
            }

            textString?.let { string ->
                val textStringWidth = textPaint.measureText(string)
                xText = width - Res.dp(9) - width * 0.45f + (width * 0.45f - textStringWidth) * 0.5f
                yText = (height - textPaint.descent() - textPaint.ascent()) / 2
            }

            drawable?.let { d ->
                d.setBounds(
                    ((width - d.intrinsicWidth) * 0.5f).roundToInt(),
                    ((height - d.intrinsicHeight) * 0.5f).roundToInt(),
                    ((width + d.intrinsicWidth) * 0.5f).roundToInt(),
                    ((height + d.intrinsicHeight) * 0.5f).roundToInt()
                )
            }
        }

        private companion object {

            private val MinHeight = Res.dp(47)
            private val MinWidth = Res.dp(109)
        }
    }
}