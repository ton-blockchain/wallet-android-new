package org.ton.wallet.feature.wallet.impl.main

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import androidx.core.math.MathUtils.clamp
import androidx.core.view.*
import androidx.recyclerview.widget.*
import org.ton.wallet.core.Res
import org.ton.wallet.core.ThreadUtils
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.coreui.ext.setOnClickListenerWithLock
import org.ton.wallet.data.notifications.api.NotificationsRepository
import org.ton.wallet.domain.transactions.api.model.TransactionDataUiListItem
import org.ton.wallet.feature.wallet.impl.main.adapter.*
import org.ton.wallet.screen.controller.BaseViewModelController
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.util.ClipboardController
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitDimen
import pub.devrel.easypermissions.EasyPermissions
import kotlin.math.max
import kotlin.math.roundToInt

class MainScreenController(args: Bundle?) : BaseViewModelController<MainScreenViewModel>(args),
    MainHeaderAdapter.HeaderItemCallback,
    MainTransactionsAdapter.AdapterCallback {

    override val viewModel by viewModels { MainScreenViewModel() }
    override val useBottomInsetsPadding = false

    private val clipboardController: ClipboardController by inject()
    private val notificationsRepository: NotificationsRepository by inject()

    private val headerAdapter = MainHeaderAdapter(this, clipboardController)
    private val emptyAdapter = MainEmptyAdapter(clipboardController)
    private val transactionsAdapter = MainTransactionsAdapter(this)
    private val concatAdapterConfig = ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build()
    private val concatAdapter = ConcatAdapter(concatAdapterConfig, headerAdapter)

    private val bottomSheetDrawable = MainBottomSheetDrawable(Res.context)
    private lateinit var firstOpenAnimationController: MainScreenStartAnimationDelegate

    private var _binding: MainScreenControllerBinding? = null
    private val binding get() = _binding!!

    private var maxBottomSheetOffset = 0f
    private var prevBalanceState: String? = ""
    private var transactionsState = MainScreenTransactionsState.Loading

    init {
        val animationOffset = (Res.screenHeight - Res.dimenInt(RUiKitDimen.splash_bottom_sheet_top) - Res.dp(100)) * 0.5f
        bottomSheetDrawable.setAnimationOffset(animationOffset)
        bottomSheetDrawable.setColor(Res.color(RUiKitColor.common_white))
        bottomSheetDrawable.setTopRadius(Res.dp(12f))
    }

    override fun onPreCreateView() {
        super.onPreCreateView()
        firstOpenAnimationController = MainScreenStartAnimationDelegate()
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        _binding = MainScreenControllerBinding(container)
        binding.scanButton.setOnClickListenerWithLock(viewModel::onScanClicked)
        binding.settingsButton.setOnClickListenerWithLock(viewModel::onSettingsClicked)
        binding.pullToRefreshLayout.setPullToRefreshListener(viewModel::onRefresh)

        val layoutManager = object : LinearLayoutManager(context) {
            override fun onLayoutCompleted(state: RecyclerView.State?) {
                super.onLayoutCompleted(state)
                recyclerScrollChangeListener.onLayoutCompleted(this)
            }
        }
        layoutManager.recycleChildrenOnDetach = true

        binding.recyclerView.adapter = concatAdapter
        binding.recyclerView.addItemDecoration(recyclerDecoration)
        binding.recyclerView.addOnScrollListener(recyclerScrollChangeListener)
        binding.recyclerView.isNestedScrollingEnabled = false
        binding.recyclerView.itemAnimator = null
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.setRecycledViewPool(MainScreenAdapterHolder.viewPool)

        setStatusBarLight(false)
        setNavigationBarLight(true)
        return binding.root
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        if (BitmapForAnimation == null) {
            onStartAnimationFinished()
        } else {
            firstOpenAnimationController.run(binding.root, binding.toolbarLayout, binding.recyclerView, bottomSheetDrawable, ::onStartAnimationFinished)
        }

        viewModel.headerStateFlow.launchInViewScope(::onHeaderStatusChanged)
        viewModel.transactionsFlow.launchInViewScope(::onItemsLoaded)
        viewModel.showNotificationPermissionFlow.launchInViewScope(::showNotificationPermission)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        binding.recyclerView.scrollToPosition(0)
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        val superInsets = super.onApplyWindowInsets(v, insets)
        val systemBarsInsets = superInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        maxBottomSheetOffset = getMaxDrawableOffset(superInsets)
        headerAdapter.height = maxBottomSheetOffset.roundToInt()
        emptyAdapter.height = Res.screenHeight - binding.toolbarLayout.layoutParams.height - headerAdapter.height - systemBarsInsets.top - systemBarsInsets.bottom
        binding.recyclerView.updatePadding(bottom = systemBarsInsets.bottom)
        return superInsets
    }

    override fun onDestroyView(view: View) {
        binding.recyclerView.removeOnScrollListener(recyclerScrollChangeListener)
        firstOpenAnimationController.reset()
        _binding = null
        super.onDestroyView(view)
    }

    // MainHeaderAdapter.HeaderItemCallback
    override fun onReceiveClicked() {
        viewModel.onReceiveClicked()
    }

    override fun onSendClicked() {
        viewModel.onSendClicked()
    }

    // MainTransactionsAdapter.AdapterCallback
    override fun onTransactionClicked(transaction: TransactionDataUiListItem) {
        viewModel.onTransactionClicked(transaction)
    }

    private fun onHeaderStatusChanged(state: MainScreenState) {
        binding.toolbarStatusText.text = when (state.headerState) {
            MainScreenHeaderState.Connecting -> Res.str(RString.status_connecting)
            MainScreenHeaderState.Updating -> Res.str(RString.status_updating)
            MainScreenHeaderState.WaitingNetwork -> Res.str(RString.status_waiting_network)
            else -> null
        }
        val isToolbarContentHidden = !binding.toolbarStatusText.text.isNullOrEmpty()
        binding.toolbarAnimationView.isInvisible = isToolbarContentHidden
        binding.toolbarBalanceText.isInvisible = isToolbarContentHidden
        binding.toolbarFiatBalanceText.isInvisible = isToolbarContentHidden
        binding.toolbarFiatBalanceText.text = state.fiatBalanceString

        headerAdapter.setAddress(state.address)
        emptyAdapter.setAddress(state.address)

        val balanceString =
            if (state.tonBalance == null || state.tonBalance == -1L) null
            else Formatter.getFormattedAmount(state.tonBalance)
        binding.toolbarBalanceText.text = balanceString
        headerAdapter.setBalance(balanceString, prevBalanceState == null && balanceString != null)
        prevBalanceState = balanceString
    }

    private fun onItemsLoaded(items: List<Any>?) {
        val newState =
            if (items == null) MainScreenTransactionsState.Loading
            else if (items.isEmpty()) MainScreenTransactionsState.Empty
            else MainScreenTransactionsState.Data

        when (newState) {
            MainScreenTransactionsState.Loading -> {
                bottomSheetDrawable.showAnimation()
                concatAdapter.removeAdapter(emptyAdapter)
                concatAdapter.removeAdapter(transactionsAdapter)
            }
            MainScreenTransactionsState.Empty -> {
                bottomSheetDrawable.hideAnimation()
                concatAdapter.removeAdapter(transactionsAdapter)
                concatAdapter.addAdapter(emptyAdapter)
                emptyAdapter.animate(true)
            }
            MainScreenTransactionsState.Data -> {
                bottomSheetDrawable.hideAnimation()
                concatAdapter.removeAdapter(emptyAdapter)
                concatAdapter.addAdapter(transactionsAdapter)
            }
        }

        transactionsAdapter.setItemsWithAnimation(items ?: emptyList(), newState == MainScreenTransactionsState.Data && transactionsState != newState)
        if (transactionsAdapter.itemCount > 0) {
            ThreadUtils.postOnMain(::updateRecyclerViewBottomOffset, 64)
        }

        transactionsState = newState
    }

    private fun updateRecyclerViewBottomOffset() {
        val binding = _binding ?: return
        val extent = binding.recyclerView.computeVerticalScrollExtent()
        val range = binding.recyclerView.computeVerticalScrollRange()
        if (extent < range && range < headerAdapter.height + extent) {
            recyclerDecoration.setLastItemBottomOffset(headerAdapter.height + extent - range)
        } else {
            recyclerDecoration.setLastItemBottomOffset(0)
        }
        binding.recyclerView.invalidateItemDecorations()
        binding.recyclerView.requestLayout()
    }

    private fun showNotificationPermission(unit: Unit) {
        val request = notificationsRepository.getPermissionRequest(activity!!, PermissionRequestIdNotifications)
        request?.let(EasyPermissions::requestPermissions)
    }

    private fun onStartAnimationFinished() {
        maxBottomSheetOffset = getMaxDrawableOffset(lastInsets)
        bottomSheetDrawable.setBitmap(null)
        bottomSheetDrawable.setTopOffset(maxBottomSheetOffset)
        binding.root.foreground = null
        binding.recyclerView.background = bottomSheetDrawable
    }

    private fun getMaxDrawableOffset(insets: WindowInsetsCompat): Float {
        val systemBarsInets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        return Res.dimen(RUiKitDimen.splash_bottom_sheet_top) - (systemBarsInets.top + (binding.pullToRefreshLayout.layoutParams as ViewGroup.MarginLayoutParams).topMargin)
    }

    private val recyclerScrollChangeListener = object : RecyclerView.OnScrollListener() {

        private val childSizeMap = hashMapOf<Int, Int>()

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (firstOpenAnimationController.isAnimating) {
                return
            }

            val totalOffset = getVerticalScrollOffset(recyclerView.layoutManager as LinearLayoutManager)

            // bottom sheet
            val topOffset = max(0f, maxBottomSheetOffset - totalOffset)
            bottomSheetDrawable.setTopOffset(topOffset)

            // toolbar
            val toolbarAlpha = clamp((totalOffset.toFloat() * 2f - headerAdapter.height * 0.35f) / headerAdapter.height, 0f, 1f)
            binding.toolbarAnimationView.alpha = toolbarAlpha
            binding.toolbarBalanceText.alpha = toolbarAlpha
            binding.toolbarFiatBalanceText.alpha = toolbarAlpha
            if (toolbarAlpha == 0f) {
                binding.toolbarAnimationView.stopAnimation()
            } else {
                binding.toolbarAnimationView.playAnimation()
            }
        }

        fun onLayoutCompleted(layoutManager: LinearLayoutManager) {
            for (i in 0 until layoutManager.childCount) {
                val childView = layoutManager.getChildAt(i)
                if (childView != null) {
                    childSizeMap[layoutManager.getPosition(childView)] = childView.height
                }
            }
        }

        private fun getVerticalScrollOffset(layoutManager: LinearLayoutManager): Int {
            if (layoutManager.childCount == 0) {
                return 0
            }
            val firstChild = layoutManager.getChildAt(0) ?: return 0
            val firstChildPosition = layoutManager.getPosition(firstChild)
            var offset = -firstChild.y.toInt()
            for (i in 0 until firstChildPosition) {
                offset += childSizeMap[i] ?: 0
            }
            return offset
        }
    }

    private val recyclerDecoration = object : RecyclerView.ItemDecoration() {

        private var lastItemBottom = 0

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            if (position == (parent.adapter?.itemCount ?: -1) - 1) {
                outRect.bottom = lastItemBottom
            }
        }

        fun setLastItemBottomOffset(offset: Int) {
            lastItemBottom = offset
        }
    }

    companion object {

        var BitmapForAnimation: Bitmap? = null

        private const val PermissionRequestIdNotifications = 0
    }
}