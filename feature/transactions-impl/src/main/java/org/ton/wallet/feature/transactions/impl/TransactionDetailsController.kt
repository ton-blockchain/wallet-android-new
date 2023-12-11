package org.ton.wallet.feature.transactions.impl

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.core.Res
import org.ton.wallet.core.ThreadUtils
import org.ton.wallet.coreui.ext.setOnClickListenerWithLock
import org.ton.wallet.domain.transactions.api.model.TransactionDetailsState
import org.ton.wallet.screen.controller.BaseViewModelBottomSheetController
import org.ton.wallet.screen.getScreenArguments
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.uicomponents.BottomSheetHelper
import org.ton.wallet.uicomponents.vh.LineDividerItemDecoration
import org.ton.wallet.uicomponents.vh.SettingsTextUiItem
import org.ton.wallet.uicomponents.view.AppToolbar
import org.ton.wallet.uikit.RUiKitDrawable
import org.ton.wallet.uikit.RUiKitStyle

class TransactionDetailsController(args: Bundle?) : BaseViewModelBottomSheetController<TransactionDetailsViewModel>(args),
    TransactionDetailsAdapter.Callback {

    private val screenArguments: TransactionDetailsScreenArguments = args.getScreenArguments()
    
    override val viewModel by viewModels { TransactionDetailsViewModel(screenArguments) }
    override val isFullHeight: Boolean = screenArguments.isMultiMessage

    private val adapter = TransactionDetailsAdapter(this)

    private lateinit var recyclerView: RecyclerView
    private lateinit var button: TextView
    private lateinit var bottomShadowView: View

    override fun createBottomSheetView(inflater: LayoutInflater, container: ViewGroup?, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_transaction_details, container, false)
        view.minimumHeight = Res.dp(300)
        view.setOnClickListener { }

        recyclerView = view.findViewById(R.id.transactionRecyclerView)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(LineDividerItemDecoration())
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)

        button = view.findViewById(R.id.transactionButton)
        button.setOnClickListenerWithLock { viewModel.onButtonClicked(activity!!) }

        bottomShadowView = view.findViewById(R.id.transactionBottomShadow)

        val toolbar = view.findViewById<AppToolbar>(R.id.transactionToolbar)
        BottomSheetHelper.connectAppToolbarWithScrollableView(toolbar, recyclerView)

        return view
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        viewModel.stateFlow.launchInViewScope(::onStateChanged)
    }

    override fun onTextItemClicked(item: SettingsTextUiItem) {
        viewModel.onTextItemClicked(activity!!, item)
    }

    private fun onStateChanged(state: TransactionDetailsState) {
        adapter.setItems(state.adapterItems)
        ThreadUtils.postOnMain({
            bottomShadowView.isVisible = recyclerView.canScrollVertically(1) || recyclerView.canScrollVertically(-1)
        }, 32)

        button.text = state.buttonTitle
        if (state.isButtonStylePrimary) {
            button.setBackgroundResource(RUiKitDrawable.bkg_button_primary)
            button.setTextAppearance(RUiKitStyle.Button_Big_Primary)
        } else {
            button.setBackgroundResource(RUiKitDrawable.bkg_button_secondary)
            button.setTextAppearance(RUiKitStyle.Button_Big_Secondary)
        }
    }
}