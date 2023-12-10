package org.ton.wallet.uicomponents.vh

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.core.Res
import org.ton.wallet.lib.lists.RecyclerHolder
import org.ton.wallet.uikit.RUiKitDrawable

object SectionDividerItem

class SectionDividerViewHolder(parent: ViewGroup) : RecyclerHolder<Unit>(View(parent.context)) {

    init {
        itemView.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, Res.dp(12))
        itemView.setBackgroundResource(RUiKitDrawable.bkg_section_divider)
    }
}

