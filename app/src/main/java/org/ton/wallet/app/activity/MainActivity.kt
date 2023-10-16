package org.ton.wallet.app.activity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.result.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.ton.wallet.app.Injector
import org.ton.wallet.app.navigation.ConductorNavigator
import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.app.util.AppIntentUtils
import org.ton.wallet.core.Res
import org.ton.wallet.core.ThreadUtils
import org.ton.wallet.di.injectScope
import org.ton.wallet.screen.controller.BaseController
import org.ton.wallet.screen.viewmodel.appViewModels
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarControllerImpl
import org.ton.wallet.uikit.RUiKitColor

class MainActivity : BaseViewModelActivity<MainActivityViewModel>(), ActivityResultCallback<Uri?> {

    override val mainViewModel by appViewModels { MainActivityViewModel() }
    private val pollingViewModel by appViewModels { PollingViewModel() }

    private val navigator: Navigator by injectScope(Injector.appDiScope)
    private val conductorNavigator: ConductorNavigator by lazy { navigator as ConductorNavigator }
    private val snackBarController: SnackBarController by injectScope(Injector.appDiScope)

    private lateinit var insetsController: WindowInsetsControllerCompat
    private lateinit var imagePickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    private var onBoardingAnimationDelegate: OnBoardingAnimationDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootLayout = FrameLayout(this)
        setContentView(rootLayout)

        pollingViewModel.start()

        // setup window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        // setup animation
        if (savedInstanceState == null) {
            val animationType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || mainViewModel.isNeedShowPassCode) {
                MainActivityAnimationType.None
            } else {
                MainActivityAnimationType.BottomSheetUp
            }

            insetsController = WindowInsetsControllerCompat(window, rootLayout)
            if (animationType == MainActivityAnimationType.None) {
                insetsController.isAppearanceLightStatusBars = true
                insetsController.isAppearanceLightNavigationBars = true
                window.setBackgroundDrawable(ColorDrawable(Res.color(RUiKitColor.common_white)))
                mainViewModel.onAnimationFinished()
            } else {
                insetsController.isAppearanceLightStatusBars = true
                insetsController.isAppearanceLightNavigationBars = true
                onBoardingAnimationDelegate = OnBoardingAnimationDelegate(rootLayout, animationType)
            }
        }

        (snackBarController as SnackBarControllerImpl).setRootLayout(rootLayout)
        conductorNavigator.attach(this, rootLayout, savedInstanceState)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia(), this)
        mainViewModel.setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        ThreadUtils.postOnMain({
            val animationType = onBoardingAnimationDelegate?.animationType ?: MainActivityAnimationType.None
            onBoardingAnimationDelegate?.startOpenAnimation {
                if (animationType == MainActivityAnimationType.BottomSheetDown) {
                    window.setBackgroundDrawable(ColorDrawable(Res.color(RUiKitColor.common_black)))
                } else if (animationType == MainActivityAnimationType.BottomSheetUp) {
                    window.setBackgroundDrawable(ColorDrawable(Res.color(RUiKitColor.common_white)))
                }
                mainViewModel.onAnimationFinished()
            }
            onBoardingAnimationDelegate = null
        }, 64)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mainViewModel.setIntent(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        navigator.forEachScreen { it.onRequestPermissionsResult(requestCode, permissions, grantResults) }
    }

    override fun onDestroy() {
        (snackBarController as SnackBarControllerImpl).onDestroy()
        conductorNavigator.detach()
        super.onDestroy()
    }

    override fun onActivityResult(result: Uri?) {
        AppIntentUtils.isAppIntentStarted = false
        navigator.forEachScreen { (it as? BaseController)?.onActivityResultCallback(result) }
    }

    fun showImagePicker(mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType) {
        AppIntentUtils.isAppIntentStarted = true
        imagePickerLauncher.launch(PickVisualMediaRequest(mediaType))
    }

    companion object {

        const val ArgumentKeyTonConnectAction = "tonConnectAction"
    }
}