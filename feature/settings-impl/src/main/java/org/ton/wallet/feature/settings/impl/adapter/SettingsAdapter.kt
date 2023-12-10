package org.ton.wallet.feature.settings.impl.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.lib.lists.diff.DiffUtilObjectCallback
import org.ton.wallet.lib.lists.diff.DiffUtilRecyclerAdapter
import org.ton.wallet.uicomponents.vh.*

class SettingsAdapter(
    private val callback: SettingsAdapterCallback
) : DiffUtilRecyclerAdapter<Any, RecyclerView.ViewHolder>(DiffUtilObjectCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewTypeHeader -> SettingsHeaderViewHolder(parent)
            ViewTypeText -> SettingsTextViewHolder(parent, callback)
            ViewTypeSwitch -> SettingsSwitchViewHolder(parent, callback)
            else -> throw IllegalArgumentException("Unsupported viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItemAt(position)) {
            is SettingsHeaderItem -> ViewTypeHeader
            is SettingsTextUiItem -> ViewTypeText
            is SettingsSwitchItem -> ViewTypeSwitch
            else -> super.getItemViewType(position)
        }
    }

    companion object {
        const val ViewTypeHeader = 0
        const val ViewTypeText = 1
        const val ViewTypeSwitch = 2
    }

    interface SettingsAdapterCallback : SettingsTextItemCallback,
        SettingsSwitchItemCallback
}