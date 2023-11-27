package org.ton.wallet.feature.wallet.impl.main.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.core.Res
import org.ton.wallet.core.ext.weak
import org.ton.wallet.feature.wallet.impl.R
import org.ton.wallet.feature.wallet.impl.main.MainScreenAdapterHolder
import org.ton.wallet.lib.lists.RecyclerAdapter
import org.ton.wallet.lib.lists.RecyclerHolder
import org.ton.wallet.rlottie.RLottieImageView
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.util.ClipboardController
import kotlin.math.max

class MainEmptyAdapter(
    var clipboardController: ClipboardController?
) : RecyclerAdapter<MainEmptyAdapter.MainEmptyItem, MainEmptyAdapter.ViewHolder>() {

    private val item = MainEmptyItem()
    private var isAnimationPlayed = false
    private var isAppearanceAnimation: Boolean? = null
    private var holderRef = weak<RecyclerView.ViewHolder?>(null)
    var height = 0

    init {
        setItems(listOf(item))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(parent, height, clipboardController)
        holderRef = weak(viewHolder)
        isAppearanceAnimation?.let { appearance -> performAnimation(viewHolder.itemView, appearance) }
        return viewHolder
    }

    override fun getItemViewType(position: Int): Int {
        return MainScreenAdapterHolder.ViewTypeMainEmpty
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (isAnimationPlayed) {
            holder.setEndState()
        } else {
            holder.playAnimation()
        }
        isAnimationPlayed = true
    }

    fun setAddress(address: String?) {
        item.address = address
        notifyItemChanged(0)
    }

    fun animate(appearance: Boolean) {
        isAppearanceAnimation = appearance
        val view = holderRef.get()?.itemView ?: return
        performAnimation(view, appearance)
    }

    private fun performAnimation(view: View, appearance: Boolean) {
        if (appearance) {
            view.alpha = 0f
            view.animate()
                .alpha(1f)
                .setDuration(250L)
                .start()
        }
    }

    class ViewHolder(
        parent: ViewGroup,
        height: Int,
        private val clipboardController: ClipboardController?
    ) : RecyclerHolder<MainEmptyItem>(R.layout.item_main_empty, parent), View.OnClickListener {

        private val animationView: RLottieImageView = itemView.findViewById(R.id.itemMainEmptyAnimationView)
        private val addressText: TextView = itemView.findViewById(R.id.itemMainEmptyAddressText)

        init {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(Res.screenWidth, View.MeasureSpec.AT_MOST)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            itemView.measure(widthSpec, heightSpec)
            itemView.layoutParams.height = max(height, itemView.measuredHeight)
            addressText.setOnClickListener(this)
        }

        override fun bind(item: MainEmptyItem) {
            super.bind(item)
            val size = item.address?.length ?: 0
            addressText.text = item.address?.replaceRange(size / 2, size / 2, "\n")
        }

        override fun onClick(v: View?) {
            clipboardController?.copyToClipboard(item.address ?: "", Res.str(RString.address_copied_to_clipboard))
        }

        fun playAnimation() {
            animationView.playAnimation()
        }

        fun setEndState() {
            animationView.setFinalFrame()
        }
    }

    class MainEmptyItem(var address: String? = null)
}