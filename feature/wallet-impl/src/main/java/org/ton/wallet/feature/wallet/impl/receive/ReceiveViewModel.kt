package org.ton.wallet.feature.wallet.impl.receive

import android.app.Activity
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.ton.wallet.core.Res
import org.ton.wallet.data.core.link.LinkUtils
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase
import org.ton.wallet.feature.wallet.api.ReceiveScreenApi
import org.ton.wallet.lib.qrbuilder.QrCodeBuilder
import org.ton.wallet.screen.viewmodel.BaseViewModel
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.util.ClipboardController
import org.ton.wallet.uikit.*

class ReceiveViewModel : BaseViewModel() {

    private val clipboardController: ClipboardController by inject()
    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase by inject()
    private val screenApi: ReceiveScreenApi by inject()

    val addressFlow: StateFlow<String> = getCurrentAccountDataUseCase.getAddressFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _qrBitmapFlow = MutableStateFlow<Bitmap?>(null)
    val qrBitmapFlow: Flow<Bitmap?> = _qrBitmapFlow

    init {
        addressFlow.filter { it.isNotEmpty() }
            .take(1)
            .onEach(::generateBitmap)
            .launchIn(viewModelScope)
    }

    fun onAddressClicked() {
        clipboardController.copyToClipboard(addressFlow.value, Res.str(RString.address_copied_to_clipboard))
    }

    fun onShareClicked(activity: Activity) {
        screenApi.shareText(activity, addressFlow.value, Res.str(RString.share_your_ton_address))
    }

    private fun generateBitmap(address: String) {
        if (address.isEmpty()) {
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            val size = Res.dimenInt(RUiKitDimen.qr_image_size)
            val content = LinkUtils.getTransferLink(address)
            val bitmap = QrCodeBuilder(content, size, size)
                .setBackgroundColor(Res.color(RUiKitColor.common_white))
                .setFillColor(Res.color(RUiKitColor.common_black))
                .setCutoutDrawable(Res.drawable(RUiKitDrawable.ic_gem_large))
                .setWithCutout(true)
                .build()
            _qrBitmapFlow.tryEmit(bitmap)
        }
    }
}