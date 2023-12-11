package org.ton.wallet.feature.transactions.impl

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.feature.transactions.impl.vh.TransactionDetailsHeaderItem
import org.ton.wallet.feature.transactions.impl.vh.TransactionDetailsHeaderViewHolder
import org.ton.wallet.lib.lists.RecyclerAdapter
import org.ton.wallet.uicomponents.vh.*
import javax.security.auth.callback.Callback

internal class TransactionDetailsAdapter(
    private val callback: Callback
) : RecyclerAdapter<Any, RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewTypeHeader -> TransactionDetailsHeaderViewHolder(parent)
            ViewTypeSectionHeader -> SettingsHeaderViewHolder(parent)
            ViewTypeSettingsItem -> SettingsTextViewHolder(parent, callback)
            ViewTypeSectionDivider -> SectionDividerViewHolder(parent)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItemAt(position)) {
            is TransactionDetailsHeaderItem -> ViewTypeHeader
            is SettingsHeaderItem -> ViewTypeSectionHeader
            is SettingsTextUiItem -> ViewTypeSettingsItem
            is SectionDividerItem -> ViewTypeSectionDivider
            else -> throw IllegalArgumentException("Unknown item type: $item")
        }
    }

    private companion object {
        private const val ViewTypeHeader = 0
        private const val ViewTypeSectionHeader = 1
        private const val ViewTypeSettingsItem = 2
        private const val ViewTypeSectionDivider = 3
    }

    interface Callback : SettingsTextItemCallback
}