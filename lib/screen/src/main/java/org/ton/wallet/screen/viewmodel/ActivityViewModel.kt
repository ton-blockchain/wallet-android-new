package org.ton.wallet.screen.viewmodel

abstract class ActivityViewModel : BaseViewModel() {

    open fun onCreate(isNewInstance: Boolean) = Unit

    open fun onStart() = Unit

    open fun onStop() = Unit
}