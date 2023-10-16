package org.ton.wallet.screen.controller

import org.ton.wallet.screen.viewmodel.BaseViewModel

interface ViewModelHolder<VM : BaseViewModel> {

    val viewModel: VM
}
