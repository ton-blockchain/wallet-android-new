package org.ton.wallet.feature.settings.impl.adapter

import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.uicomponents.vh.LineDividerItemDecoration

internal class SettingsItemDecoration : LineDividerItemDecoration() {

    override fun isNeedDrawDivider(adapter: RecyclerView.Adapter<*>, position: Int): Boolean {
        if (position == 0 || position == adapter.itemCount - 1) {
            return false
        }
        val currViewType = adapter.getItemViewType(position)
        val nextViewType = adapter.getItemViewType(position + 1)
        return super.isNeedDrawDivider(adapter, position) || isTextViewType(currViewType) && isTextViewType(nextViewType)
    }

    private fun isTextViewType(viewType: Int): Boolean {
        return viewType == SettingsAdapter.ViewTypeText || viewType == SettingsAdapter.ViewTypeSwitch
    }
}