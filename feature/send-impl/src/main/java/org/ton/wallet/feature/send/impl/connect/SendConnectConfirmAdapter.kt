package org.ton.wallet.feature.send.impl.connect

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.feature.send.impl.connect.vh.*
import org.ton.wallet.lib.lists.RecyclerAdapter
import org.ton.wallet.uicomponents.vh.*

class SendConnectConfirmAdapter(
    private val callback: Callback
) : RecyclerAdapter<Any, RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewTypeHeader -> SendConnectHeaderViewHolder(parent)
            ViewTypeSettings -> SettingsTextViewHolder(parent, null)
            ViewTypeSectionDivider -> SectionDividerViewHolder(parent)
            ViewTypeShowDetails -> ShowDetailsViewHolder(parent, callback)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItemAt(position)) {
            is SendConnectHeaderItem -> ViewTypeHeader
            is SettingsTextUiItem -> ViewTypeSettings
            is SectionDividerItem -> ViewTypeSectionDivider
            is ShowDetailsItem -> ViewTypeShowDetails
            else -> throw IllegalArgumentException("Unknown item type: $item")
        }
    }

    private companion object {

        private const val ViewTypeHeader = 0
        private const val ViewTypeSettings = 1
        private const val ViewTypeSectionDivider = 2
        private const val ViewTypeShowDetails = 3
    }

    interface Callback : ShowDetailsViewHolder.Callback
}