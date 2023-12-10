package org.ton.wallet.coreui

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import org.ton.wallet.core.Res
import org.ton.wallet.core.ext.*
import org.ton.wallet.coreui.util.FontSpan
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow

object Formatter {

    const val BeatifiedAmountSmallFactor = 0.73f

    private val stringBuilder = threadLocalSafe { StringBuilder() }

    private val decimalFormat by lazy { DecimalFormat.getInstance(locale) as DecimalFormat }
    private val locale by lazy { Locale.US }

    private val date = threadLocal { Date() }
    private val dayMonthFormat by lazy { SimpleDateFormat("MMMM d", locale) }
    private val fullDateFormat by lazy { SimpleDateFormat("MMM d, yyyy", locale) }
    private val timeFormat by lazy { SimpleDateFormat("HH:mm", locale) }

    val decimalSeparator: Char
        get() = decimalFormat.decimalFormatSymbols.decimalSeparator

    fun getBeautifiedAmount(amount: CharSequence?, proportion: Float = BeatifiedAmountSmallFactor): SpannableStringBuilder? {
        if (amount == null) {
            return null
        }
        val separatorPosition = amount.indexOf(decimalSeparator)
        if (separatorPosition == -1) {
            return SpannableStringBuilder(amount)
        }
        val spannableBuilder = SpannableStringBuilder(amount)
        spannableBuilder.setSpan(RelativeSizeSpan(proportion), separatorPosition, amount.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannableBuilder
    }

    fun getFormattedAmount(amount: Long, isApproximate: Boolean = false): String {
        val divider = 10.0.pow(9.0).toLong()
        val integerPart = amount / divider
        val decimalPart = amount % divider
        if (decimalPart == 0L) {
            return String.format(locale, "%d", integerPart)
        }
        var pow = 10L
        var decimals = 9
        for (i in 9 downTo 1) {
            if (decimalPart % pow != 0L) {
                break
            }
            pow *= 10
            decimals--
        }

        val formatBuilder = stringBuilder.getSafe().clear()
            .apply {
                if (isApproximate) {
                    append("≈ ")
                }
            }
            .append("%d.%0")
            .append(decimals)
            .append('d')
        return String.format(locale, formatBuilder.toString(), abs(integerPart), abs(decimalPart / (pow / 10)))
    }

    fun getFormattedAmount(balance: BigDecimal, currencySymbol: String, isApproximate: Boolean = false): String {
        var value = balance.stripTrailingZeros()
        if (value.scale() > 0) {
            value = value.setScale(2, RoundingMode.HALF_UP)
        }

        val decimals = value.scale()
        val format = stringBuilder.getSafe().clear()
            .apply {
                if (isApproximate) {
                    append("≈ ")
                }
            }
            .append("%s%.")
            .append(decimals)
            .append('f')
            .toString()
        return String.format(Res.getCurrentLocale(), format, currencySymbol, balance)
    }

    fun getShortAddress(address: String): String {
        return getShortAddressSafe(address)!!
    }

    fun getShortAddressSafe(address: String?): String? {
        return address?.hiddenMiddle(4, 4)
    }

    fun getMiddleAddress(address: String?): String? {
        return address?.hiddenMiddle(6, 7)
    }

    fun getShortHash(hash: String?): String? {
        return hash?.hiddenMiddle(6, 6)
    }

    fun getBeautifiedShortString(shortString: CharSequence, font: Typeface): SpannableStringBuilder {
        return getBeautifiedShortStringSafe(shortString, font)!!
    }

    fun getBeautifiedShortStringSafe(shortString: CharSequence?, font: Typeface): SpannableStringBuilder? {
        if (shortString.isNullOrEmpty()) {
            return null
        }
        val stringBuilder = SpannableStringBuilder(shortString)
        stringBuilder.setSpan(
            FontSpan(font),
            shortString.indexOfFirst { it == '.' },
            shortString.indexOfLast { it == '.' } + 1,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        return stringBuilder
    }

    fun getTimeString(timestampMs: Long): String {
        val date = date.get()!!
        date.time = timestampMs
        return timeFormat.format(date)
    }

    fun getDayMonthString(timestampMs: Long): String {
        val date = date.get()!!
        date.time = timestampMs
        return dayMonthFormat.format(date)
    }

    fun getFullDateString(timestampMs: Long): String {
        val date = date.get()!!
        date.time = timestampMs
        return fullDateFormat.format(date)
    }
}