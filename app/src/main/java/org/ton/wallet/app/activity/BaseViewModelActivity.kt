package org.ton.wallet.app.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.ton.wallet.screen.controller.TargetResultHandler
import org.ton.wallet.screen.viewmodel.ActivityViewModel

abstract class BaseViewModelActivity<VM : ActivityViewModel> : AppCompatActivity(), TargetResultHandler {

    abstract val mainViewModel: VM

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mainViewModel.onCreate(savedInstanceState == null)
    }

    override fun onStart() {
        super.onStart()
        mainViewModel.onStart()
    }

    override fun onPostResume() {
        super.onPostResume()
        mainViewModel.onActivityResumed()
    }

    override fun onPause() {
        mainViewModel.onActivityPaused()
        super.onPause()
    }

    override fun onStop() {
        mainViewModel.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mainViewModel.onDestroy()
        super.onDestroy()
    }

    override fun onResultReceived(code: String, args: Bundle?) {
        super.onResultReceived(code, args)
        mainViewModel.onResultReceived(code, args)
    }

    override fun setTargetResult(code: String, args: Bundle?) = Unit
}