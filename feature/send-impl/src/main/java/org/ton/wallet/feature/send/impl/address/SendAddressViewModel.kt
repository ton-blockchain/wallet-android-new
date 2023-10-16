package org.ton.wallet.feature.send.impl.address

import android.app.Activity
import android.content.ClipDescription
import android.content.ClipboardManager
import android.os.Bundle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.ton.wallet.core.Res
import org.ton.wallet.core.ThreadUtils
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.coreui.KeyboardUtils
import org.ton.wallet.data.core.link.LinkAction
import org.ton.wallet.data.core.link.LinkUtils
import org.ton.wallet.domain.blockhain.api.GetAddressTypeUseCase
import org.ton.wallet.domain.transactions.api.GetRecentSendTransactionsUseCase
import org.ton.wallet.feature.scanqr.api.ScanQrScreenApi
import org.ton.wallet.feature.send.api.SendAddressScreenApi
import org.ton.wallet.screen.viewmodel.BaseViewModel
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarMessage
import org.ton.wallet.uikit.RUiKitDrawable

class SendAddressViewModel(private val args: SendAddressScreenArguments) : BaseViewModel() {

    private val getAddressTypeUseCase: GetAddressTypeUseCase by inject()
    private val getRecentSendTransactionsUseCase: GetRecentSendTransactionsUseCase by inject()
    private val screenApi: SendAddressScreenApi by inject()
    private val snackBarController: SnackBarController by inject()

    private var qrCodeProcessJob: Job? = null
    private var qrCodeProcessed = false

    private val _addressTextFlow = MutableStateFlow<String?>(null)
    val textFlow: Flow<String?> = _addressTextFlow

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: Flow<Boolean> = _isLoadingFlow

    private val _recentAddressesFlow = MutableStateFlow(emptyList<RecentAddressItem>())
    val recentAddressesFlow: Flow<List<RecentAddressItem>> = _recentAddressesFlow

    init {
        val address = args.address
        if (!address.isNullOrEmpty()) {
            _addressTextFlow.tryEmit(address)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val recent = getRecentSendTransactionsUseCase.invoke().map { dto ->
                RecentAddressItem(
                    address = dto.address,
                    shortAddress = Formatter.getShortAddress(dto.address),
                    dateString = Formatter.getDayMonthString(dto.timestampSec * 1000L)
                )
            }
            _recentAddressesFlow.tryEmit(recent)
        }
    }

    override fun onResultReceived(code: String, args: Bundle?) {
        super.onResultReceived(code, args)
        if (code == ScanQrScreenApi.ResultCodeQrDetected) {
            processQrCode(args?.getString(ScanQrScreenApi.ArgumentKeyQrValue))
        }
    }

    fun onTextChanged(text: String) {
        _addressTextFlow.tryEmit(text)
    }

    fun onPasteClicked() {
        val text = getClipboardText()
        if (text != null) {
            _addressTextFlow.tryEmit(text)
        }
    }

    fun onScanClicked() {
        qrCodeProcessed = false
        screenApi.navigateToScanQr()
    }

    fun onContinueClicked(activity: Activity) {
        if (_isLoadingFlow.value) {
            return
        }
        val address = _addressTextFlow.value?.trim() ?: ""
        onAddressSelected(activity, address)
    }

    fun onRecentItemClicked(activity: Activity, item: RecentAddressItem) {
        _addressTextFlow.tryEmit(item.address)
        onAddressSelected(activity, item.address)
    }

    private fun onAddressSelected(activity: Activity, address: String) {
        _isLoadingFlow.tryEmit(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val addressType = getAddressTypeUseCase.getAddressType(address)
                if (addressType == null) {
                    val message = SnackBarMessage(
                        title = Res.str(RString.invalid_address),
                        message = Res.str(RString.invalid_address_description),
                        drawable = Res.drawable(RUiKitDrawable.ic_warning_32)
                    )
                    snackBarController.showMessage(message)
                } else {
                    withContext(Dispatchers.Main) {
                        KeyboardUtils.hideKeyboard(activity.window) {
                            ThreadUtils.postOnMain { screenApi.navigateToSendAmount(addressType, args.amount, args.message) }
                        }
                    }
                }
            } finally {
                _isLoadingFlow.tryEmit(false)
            }
        }
    }

    private fun processQrCode(code: String?) {
        if (qrCodeProcessJob?.isActive == true || qrCodeProcessed) {
            return
        }
        qrCodeProcessJob = viewModelScope.launch(Dispatchers.Default) {
            val linkAction = LinkUtils.parseLink(code ?: "")
            if (linkAction != null && linkAction is LinkAction.TransferAction) {
                _addressTextFlow.tryEmit(linkAction.address)
                screenApi.navigateBack()
                qrCodeProcessed = true
            }
        }
    }

    private fun getClipboardText(): String? {
        val clipboardManager = Res.context.getSystemService(ClipboardManager::class.java)
        val primaryClip = clipboardManager.primaryClip ?: return null
        val isClipboardContainsText = primaryClip.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                || primaryClip.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
        if (!isClipboardContainsText) {
            return null
        }
        return primaryClip.getItemAt(0)?.text.toString()
    }
}