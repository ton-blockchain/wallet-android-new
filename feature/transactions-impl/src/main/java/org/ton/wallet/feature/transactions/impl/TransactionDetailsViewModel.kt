package org.ton.wallet.feature.transactions.impl

import android.app.Activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.ton.wallet.core.Res
import org.ton.wallet.data.transactions.api.model.TransactionStatus
import org.ton.wallet.domain.transactions.api.GetTransactionDetailsUseCase
import org.ton.wallet.domain.transactions.api.model.TransactionDetailsState
import org.ton.wallet.feature.transactions.api.TransactionDetailsScreenApi
import org.ton.wallet.lib.log.L
import org.ton.wallet.screen.viewmodel.BaseViewModel
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.util.ClipboardController

class TransactionDetailsViewModel(args: TransactionDetailsScreenArguments) : BaseViewModel() {

    private val clipboardController: ClipboardController by inject()
    private val getTransactionDetailsUseCase: GetTransactionDetailsUseCase by inject()
    private val screenApi: TransactionDetailsScreenApi by inject()

    private val _stateFlow = MutableStateFlow<TransactionDetailsState?>(null)
    val stateFlow: Flow<TransactionDetailsState?> = _stateFlow

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = try {
                getTransactionDetailsUseCase.invoke(args.internalId)
            } catch (e: Exception) {
                null
            }
            if (transaction == null) {
                L.e(IllegalArgumentException("Could not load transaction with internal id ${args.internalId}"))
                screenApi.navigateBack()
            } else {
                _stateFlow.value = transaction
                screenApi.preloadUrl(getExplorerUrl(transaction.hash))
            }
        }
    }

    fun onButtonClicked() {
        val transaction = _stateFlow.value ?: return
        val peerAddress = transaction.peerAddress ?: return
        val amount =
            if (transaction.status == TransactionStatus.Cancelled) transaction.amount
            else null
        screenApi.navigateToSendAmount(peerAddress, amount)
    }

    fun onPeerAddressClicked() {
        _stateFlow.value?.peerAddress?.let { address ->
            clipboardController.copyToClipboard(address, Res.str(RString.address_copied_to_clipboard), true)
        }
    }

    fun onHashClicked() {
        _stateFlow.value?.hash?.let { hash ->
            clipboardController.copyToClipboard(hash, Res.str(RString.transaction_hash_copied_to_clipboard), true)
        }
    }

    fun onViewExplorerClicked(activity: Activity) {
        _stateFlow.value?.hash?.let { hash ->
            screenApi.navigateToBrowser(activity, getExplorerUrl(hash))
        }
    }

    private fun getExplorerUrl(hash: String): String {
        return "https://tonscan.org/tx/$hash"
    }
}