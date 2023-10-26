package org.ton.wallet.feature.wallet.impl.receive

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import org.ton.wallet.coreui.ext.setOnClickListenerWithLock
import org.ton.wallet.feature.wallet.impl.R
import org.ton.wallet.screen.controller.BaseViewModelBottomSheetController
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.uicomponents.BottomSheetHelper

class ReceiveController(args: Bundle?) : BaseViewModelBottomSheetController<ReceiveViewModel>(args) {

    override val viewModel by viewModels { ReceiveViewModel() }

    private lateinit var qrImageView: ImageView
    private lateinit var addressText: TextView

    override fun createBottomSheetView(inflater: LayoutInflater, container: ViewGroup?, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_receive, container, false)
        BottomSheetHelper.connectAppToolbarWithScrollableView(
            toolbar = view.findViewById(R.id.receiveToolbar),
            view = view.findViewById(R.id.receiveScrollView)
        )
        view.findViewById<View>(R.id.receiveShareButton).setOnClickListenerWithLock { viewModel.onShareClicked(activity!!) }

        addressText = view.findViewById(R.id.receiveAddressText)
        addressText.setOnClickListenerWithLock(viewModel::onAddressClicked)

        qrImageView = view.findViewById(R.id.receiveQrImage)

        return view
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        viewModel.addressFlow.launchInViewScope(::setAddress)
        viewModel.qrBitmapFlow.launchInViewScope(qrImageView::setImageBitmap)
    }

    private fun setAddress(address: String) {
        addressText.text = address.replaceRange(address.length / 2, address.length / 2, "\n")
    }
}