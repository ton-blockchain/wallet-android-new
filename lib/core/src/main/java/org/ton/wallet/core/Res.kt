package org.ton.wallet.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowInsetsCompat
import org.ton.wallet.core.ext.threadLocalSafe
import java.util.*
import kotlin.math.roundToInt

@SuppressLint("StaticFieldLeak")
object Res {

    private val displayMetrics = threadLocalSafe { DisplayMetrics() }
    private val tlTypedValue = threadLocalSafe { TypedValue() }

    lateinit var context: Context
        private set
    private lateinit var windowManager: WindowManager

    private var density = Resources.getSystem().displayMetrics.density

    val isLandscapeScreenSize: Boolean
        get() = screenWidth > screenHeight

    var isRtl: Boolean = false
        private set

    var screenHeight: Int = 0
        private set

    var screenWidth: Int = 0
        private set

    fun init(context: Context, configuration: Configuration) {
        this.context = context
        windowManager = context.getSystemService(WindowManager::class.java)

        density = context.resources.displayMetrics.density
        isRtl = configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        // screen size
        val displayMetrics = displayMetrics.get()
        if (displayMetrics == null) {
            screenWidth = context.resources.displayMetrics.widthPixels
            screenHeight = context.resources.displayMetrics.heightPixels
        } else {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            screenWidth = displayMetrics.widthPixels
            screenHeight = displayMetrics.heightPixels
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowInsets = windowManager.currentWindowMetrics.windowInsets
                val insetsMask = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                val insets = windowInsets.getInsetsIgnoringVisibility(insetsMask)
                screenWidth += insets.left + insets.right
                screenHeight += insets.top + insets.bottom
            }
        }
    }

    @ColorInt
    fun color(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(context, colorRes)
    }

    @Px
    fun dimen(@DimenRes dimenRes: Int): Float {
        return context.resources.getDimension(dimenRes)
    }

    @Px
    fun dimenInt(@DimenRes dimenRes: Int): Int {
        return dimen(dimenRes).roundToInt()
    }

    @Px
    fun dimenAttr(@AttrRes resId: Int): Int {
        val typedValue = tlTypedValue.get() ?: return 0
        val isResolved = context.theme.resolveAttribute(resId, typedValue, true)
        return if (isResolved) {
            TypedValue.complexToDimensionPixelSize(typedValue.data, Resources.getSystem().displayMetrics)
        } else {
            0
        }
    }

    @Px
    fun dp(dp: Int): Int {
        return dp(dp.toFloat()).roundToInt()
    }

    @Px
    fun dp(dp: Float): Float {
        return dp * density
    }

    fun drawable(@DrawableRes drawableRes: Int): Drawable {
        return ContextCompat.getDrawable(context, drawableRes)!!
    }

    fun drawableBitmap(drawable: Drawable): Bitmap {
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        canvas.setBitmap(null)
        return bitmap
    }

    fun drawableColored(@DrawableRes drawableRes: Int, @ColorInt color: Int): Drawable {
        val drawable = ContextCompat.getDrawable(context, drawableRes)!!.mutate()
        drawable.setTint(color)
        return drawable
    }

    @SuppressLint("DiscouragedApi")
    @DrawableRes
    fun drawableResId(drawableResName: String): Int {
        return context.resources.getIdentifier(drawableResName, "drawable", context.packageName)
    }

    fun font(@FontRes fontRes: Int): Typeface {
        return ResourcesCompat.getFont(context, fontRes)!!
    }

    fun fraction(@FractionRes fractionRes: Int): Float {
        return context.resources.getFraction(fractionRes, 1, 1)
    }

    fun getCurrentLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            context.resources.configuration.locale
        }
    }

    fun plural(@PluralsRes stringRes: Int, quantity: Int, vararg formatArgs: Any): String {
        return context.resources.getQuantityString(stringRes, quantity, *formatArgs)
    }

    @Px
    fun sp(sp: Int): Int {
        return sp(sp.toFloat()).roundToInt()
    }

    @Px
    fun sp(sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
    }

    fun str(@StringRes stringRes: Int): String {
        return context.getString(stringRes)
    }

    fun str(@StringRes stringRes: Int, vararg formatArgs: Any): String {
        return context.getString(stringRes, *formatArgs)
    }
}