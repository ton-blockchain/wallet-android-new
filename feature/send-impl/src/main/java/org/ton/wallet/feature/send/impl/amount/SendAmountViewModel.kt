package org.ton.wallet.feature.send.impl.amount

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.core.math.MathUtils.clamp
import kotlinx.coroutines.flow.*
import org.ton.wallet.core.Res
import org.ton.wallet.core.ThreadUtils
import org.ton.wallet.core.ext.addSpans
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.coreui.util.FontSpan
import org.ton.wallet.data.core.ton.TonCoin
import org.ton.wallet.domain.wallet.api.GetCurrentAccountBalanceUseCase
import org.ton.wallet.feature.send.api.SendAmountScreenApi
import org.ton.wallet.screen.viewmodel.BaseViewModel
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarMessage
import org.ton.wallet.uicomponents.view.NumPadView
import org.ton.wallet.uikit.*
import java.math.BigDecimal

class SendAmountViewModel(private val args: SendAmountScreenArguments) : BaseViewModel(), NumPadView.NumPadViewListener {

    private val getCurrentAccountBalanceUseCase: GetCurrentAccountBalanceUseCase by inject()
    private val screenApi: SendAmountScreenApi by inject()
    private val snackBarController: SnackBarController by inject()

    private var selectionChangedAfterText = false

    val balanceFlow: Flow<Long?> = getCurrentAccountBalanceUseCase.getTonBalanceFlow()

    private val balanceDecimalFlow: StateFlow<BigDecimal?> = balanceFlow.map { balanceLong ->
        if (balanceLong == null) null
        else BigDecimal(balanceLong).movePointLeft(TonCoin.Decimals)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _amountStringFlow = MutableStateFlow<CharSequence>("")
    val amountFlow: Flow<CharSequence> = _amountStringFlow

    private val _amountSelectionFlow = MutableStateFlow(0)
    val amountSelectionFlow: Flow<Int> = _amountSelectionFlow

    private val _isInsufficientFundsFlow = MutableStateFlow(false)
    val isInsufficientFundsFlow: Flow<Boolean> = _isInsufficientFundsFlow

    private val _isSendAllCheckedFlow = MutableStateFlow(false)
    val sendAllCheckedFlow: Flow<Boolean> = _isSendAllCheckedFlow

    val isAddressEditable = args.isAddressEditable
    val sendToString: CharSequence

    init {
        val shortAddress = Formatter.getShortAddressSafe(args.ufAddress) ?: ""
        val stringBuilder = SpannableStringBuilder(Res.str(RString.send_to, shortAddress, args.dns ?: ""))
        val index = stringBuilder.indexOf(shortAddress)
        if (index >= 0) {
            val spans = listOf(ForegroundColorSpan(Res.color(RUiKitColor.text_primary)), FontSpan(Res.font(RUiKitFont.inter_regular)))
            stringBuilder.addSpans(spans, index, index + shortAddress.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }
        sendToString = stringBuilder

        args.amount?.let { amount ->
            val formattedAmount = Formatter.getFormattedAmount(amount)
            setNewAmount(formattedAmount)
            _amountSelectionFlow.value = formattedAmount.length
        }
    }

    override fun onNumberClicked(number: Int) {
        val selection = _amountSelectionFlow.value
        val newAmount = _amountStringFlow.value.replaceRange(selection, selection, number.toString())
        val isFirstSymbolsInCorrect = newAmount.length >= 2 && newAmount[0] == '0' &&
                '0' <= newAmount[1] && newAmount[1] <= '9'
        if (!isFirstSymbolsInCorrect&& _amountStringFlow.value.length < MAX_LENGTH) {
            setNewAmount(newAmount)
            _amountSelectionFlow.value = clamp(selection + 1, 0, newAmount.length)
        }
    }

    override fun onBackSpaceClicked() {
        val selection = _amountSelectionFlow.value
        if (selection > 0) {
            setNewAmount(_amountStringFlow.value.removeRange(selection - 1, selection))
            _amountSelectionFlow.value = selection - 1
        }
    }

    override fun onBackSpaceLongClicked() {
        setNewAmount("")
        _amountSelectionFlow.value = _amountStringFlow.value.length
    }

    override fun onDotClicked() {
        if (!_amountStringFlow.value.contains(Formatter.decimalSeparator)
            && _amountSelectionFlow.value > 0
            && _amountStringFlow.value.length < MAX_LENGTH
        ) {
            val selection = _amountSelectionFlow.value
            setNewAmount(_amountStringFlow.value.replaceRange(selection, selection, Formatter.decimalSeparator.toString()))
            _amountSelectionFlow.value = selection + 1
        }
    }

    fun onEditAddressClicked() {
        screenApi.navigateBack()
    }

    fun onContinueClicked() {
        if (_isInsufficientFundsFlow.value) {
            return
        }
        val amountBigDecimal = _amountStringFlow.value.toString().toBigDecimalOrNull()?.movePointRight(TonCoin.Decimals)
        val minAmount = BigDecimal.ONE
        val maxAmount = BigDecimal.valueOf(Long.MAX_VALUE)
        if (amountBigDecimal != null && minAmount <= amountBigDecimal && amountBigDecimal <= maxAmount) {
            ThreadUtils.postOnMain {
                screenApi.navigateToConfirm(args.ufAddress, amountBigDecimal.longValueExact(), _isSendAllCheckedFlow.value, args.message)
            }
        } else {
            snackBarController.showMessage(SnackBarMessage(
                title = Res.str(RString.error),
                message = Res.str(RString.wrong_amount),
                drawable = Res.drawable(RUiKitDrawable.ic_warning_32)
            ))
        }
    }

    fun onSendAllClicked() {
        _isSendAllCheckedFlow.value = !_isSendAllCheckedFlow.value
        if (_isSendAllCheckedFlow.value) {
            val balanceDecimal = balanceDecimalFlow.value
            if (balanceDecimal != null) {
                setNewAmount(balanceDecimal.toPlainString())
                _amountSelectionFlow.value = _amountStringFlow.value.length
            }
        }
    }

    fun onTextSelectionChanged(start: Int, end: Int) {
        if (!selectionChangedAfterText && start == end) {
            _amountSelectionFlow.tryEmit(start)
        }
        selectionChangedAfterText = false
    }

    private fun setNewAmount(amount: CharSequence?) {
        selectionChangedAfterText = true
        val stringBuilder: SpannableStringBuilder? = Formatter.getBeautifiedAmount(amount)
        if (amount != null && amount.length <= 19 && stringBuilder != null) {
            val amountDecimal = try {
                if (amount.isEmpty()) BigDecimal.ZERO
                else BigDecimal(amount.toString())
            } catch (e: Exception) {
                null
            }
            val balanceDecimal = balanceDecimalFlow.value
            if (amountDecimal != null && balanceDecimal != null) {
                if (amountDecimal != balanceDecimal) {
                    _isSendAllCheckedFlow.value = false
                }
                _isInsufficientFundsFlow.value = amountDecimal > balanceDecimal
            }
            _amountStringFlow.value = stringBuilder
        }
    }

    private companion object {
        private const val MAX_LENGTH = 19
    }
}