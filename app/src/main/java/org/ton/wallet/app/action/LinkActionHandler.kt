package org.ton.wallet.app.action

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.core.Res
import org.ton.wallet.data.core.link.LinkAction
import org.ton.wallet.data.core.link.LinkActionHandler
import org.ton.wallet.domain.blockhain.api.AddressType
import org.ton.wallet.domain.blockhain.api.GetAddressTypeUseCase
import org.ton.wallet.feature.send.impl.address.SendAddressScreenArguments
import org.ton.wallet.feature.send.impl.amount.SendAmountScreenArguments
import org.ton.wallet.feature.tonconnect.impl.TonConnectApproveScreenArguments
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarMessage
import org.ton.wallet.uikit.RUiKitDrawable

internal class LinkActionHandlerImpl(
    private val navigator: Navigator,
    private val snackBarController: SnackBarController,
    private val getAddressTypeUseCase: GetAddressTypeUseCase
) : LinkActionHandler {

    override fun processLinkAction(action: LinkAction) {
        if (action is LinkAction.TransferAction) {
            processTransfer(action)
        } else if (action is LinkAction.TonConnectAction) {
            processTonConnect(action)
        }
    }

    private fun processTransfer(action: LinkAction.TransferAction) {
        val address = action.address ?: ""
        val addressType = getAddressTypeUseCase.guessAddressType(address)
        val isRawOrDnsAddress = addressType is AddressType.RawAddress || addressType is AddressType.DnsAddress
        if (addressType == null || isRawOrDnsAddress) {
            val addressArgument = if (isRawOrDnsAddress) address else null
            navigator.push(SendAddressScreenArguments(addressArgument, action.amount, action.message))
        } else {
            navigator.push(SendAmountScreenArguments(
                ufAddress = address,
                dns = null,
                amount = action.amount,
                message = action.message,
                isAddressEditable = false
            ))
        }
    }

    private fun processTonConnect(action: LinkAction.TonConnectAction) {
        if (action.version != 2) {
            snackBarController.showMessage(SnackBarMessage(
                title = Res.str(RString.error),
                message = Res.str(RString.ton_connect_unsupported_version, action.version),
                drawable = Res.drawable(RUiKitDrawable.ic_warning_32)
            ))
            return
        }
        navigator.push(TonConnectApproveScreenArguments(action.url))
    }
}