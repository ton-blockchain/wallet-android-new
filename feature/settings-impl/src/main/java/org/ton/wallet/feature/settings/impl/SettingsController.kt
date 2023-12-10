package org.ton.wallet.feature.settings.impl

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.core.math.MathUtils.clamp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.core.Res
import org.ton.wallet.feature.settings.impl.adapter.*
import org.ton.wallet.screen.controller.BaseViewModelController
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.dialog.AlertDialog
import org.ton.wallet.uicomponents.drawable.TopRoundRectDrawable
import org.ton.wallet.uicomponents.popup.BasePopupWindow
import org.ton.wallet.uicomponents.popup.MenuPopupWindow
import org.ton.wallet.uicomponents.vh.SettingsTextUiItem
import org.ton.wallet.uicomponents.vh.SettingsUiItem
import org.ton.wallet.uicomponents.view.AppToolbar
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitDimen

class SettingsController(args: Bundle?) : BaseViewModelController<SettingsViewModel>(args) {

    override val viewModel by viewModels { SettingsViewModel() }
    override val useBottomInsetsPadding = false

    private val adapterCallback = object : SettingsAdapter.SettingsAdapterCallback {

        override fun onTextItemClicked(item: SettingsTextUiItem) {
            super.onTextItemClicked(item)
            viewModel.onTextItemClicked(activity!!, item)
        }

        override fun onSwitchItemClicked(item: SettingsSwitchItem) {
            super.onSwitchItemClicked(item)
            viewModel.onSwitchItemClicked(activity!!, item)
        }
    }

    private val adapter = SettingsAdapter(adapterCallback)
    private val bottomSheetDrawable = TopRoundRectDrawable(Res.screenHeight)

    private lateinit var toolbar: AppToolbar
    private lateinit var recyclerView: RecyclerView

    private var initialDrawableOffset = 0f
    private var popupWindow: BasePopupWindow? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_settings, container, false)

        toolbar = view.findViewById(R.id.settingsToolbar)
        toolbar.setShadowAlpha(0f)

        initialDrawableOffset = Res.dimenAttr(android.R.attr.actionBarSize).toFloat()
        bottomSheetDrawable.setColor(Res.color(RUiKitColor.common_white))
        bottomSheetDrawable.setTopRadius(Res.dimen(RUiKitDimen.bottom_sheet_radius))
        bottomSheetDrawable.setTopOffset(initialDrawableOffset)
        view.findViewById<View>(R.id.settingsBackgroundView).background = bottomSheetDrawable

        recyclerView = view.findViewById(R.id.settingsRecyclerView)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(SettingsItemDecoration())
        recyclerView.addOnScrollListener(scrollListener)
        recyclerView.itemAnimator = null
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        setStatusBarLight(false)
        return view
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        viewModel.setActivity(activity as FragmentActivity)
        viewModel.itemsFlow.launchInViewScope(::onItemsChanged)
        viewModel.showDeleteWalletDialogFlow.launchInViewScope(::showDeleteWalletDialog)
        viewModel.showAddressPopupItemsFlow.launchInViewScope { items ->
            showPopupItems(items, SettingsViewModel.ItemAddress)
        }
        viewModel.showFiatCurrencyPopupItemsFlow.launchInViewScope { items ->
            showPopupItems(items, SettingsViewModel.ItemCurrency)
        }
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        val superInsets = super.onApplyWindowInsets(v, insets)
        val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        recyclerView.updatePadding(bottom = systemBarsInsets.bottom)
        return superInsets
    }

    override fun onDestroyView(view: View) {
        recyclerView.removeOnScrollListener(scrollListener)
        super.onDestroyView(view)
    }

    private fun onItemsChanged(items: List<Any>) {
        adapter.setItems(items)
    }

    private fun showDeleteWalletDialog(value: Unit) {
        val clickListener = DialogInterface.OnClickListener { dialog, which ->
            if (which == DialogInterface.BUTTON_POSITIVE) {
                viewModel.onDeleteWalletClicked()
            }
            dialog.dismiss()
        }
        val alertDialog = AlertDialog.Builder(
            title = Res.str(RString.delete_wallet_alert_title),
            message = Res.str(RString.delete_wallet_alert_description),
            positiveButton = Res.str(RString.ok) to clickListener,
            negativeButton = Res.str(RString.cancel) to clickListener
        ).build(context)
        showDialog(alertDialog)
    }

    // utils
    private fun showPopupItems(items: List<MenuPopupWindow.MenuPopupItem>, itemId: Int) {
        val viewPosition = getItemPosition(itemId)
        if (viewPosition == -1) {
            return
        }
        val view = recyclerView.findViewHolderForAdapterPosition(viewPosition)?.itemView ?: return
        showPopupItems(items, view)
    }

    private fun showPopupItems(items: List<MenuPopupWindow.MenuPopupItem>, view: View) {
        if (popupWindow != null) {
            popupWindow = null
            return
        }
        popupWindow = MenuPopupWindow(context)
            .setItems(items)
            .setDismissListener { popupWindow = null }
            .also { popupWindow = it }
            .show(view, Gravity.TOP or Gravity.END, -view.height)
    }

    private fun getItemPosition(itemId: Int): Int {
        for (i in 0 until adapter.itemCount) {
            if ((adapter.getItemAt(i) as? SettingsUiItem)?.id == itemId) {
                return i
            }
        }
        return -1
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {

        private val offsetThreshold = Res.dp(16f)
        private var cumulativeOffset = 0

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            cumulativeOffset += dy
            val topOffset = clamp(initialDrawableOffset - cumulativeOffset, initialDrawableOffset - bottomSheetDrawable.topRadius, initialDrawableOffset)
            bottomSheetDrawable.setTopOffset(topOffset)
            toolbar.setShadowAlpha(clamp(cumulativeOffset / offsetThreshold, 0f, 1f))
        }
    }
}