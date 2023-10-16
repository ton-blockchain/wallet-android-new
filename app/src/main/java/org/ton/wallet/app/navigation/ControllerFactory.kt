package org.ton.wallet.app.navigation

import android.app.Activity
import android.os.Bundle
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import org.ton.wallet.app.Injector
import org.ton.wallet.app.navigation.changehandler.BottomSheetChangeHandler
import org.ton.wallet.app.navigation.changehandler.SlideChangeHandler
import org.ton.wallet.app.util.AppErrorHandler
import org.ton.wallet.core.Res
import org.ton.wallet.core.ext.weak
import org.ton.wallet.feature.impl.receive.ReceiveController
import org.ton.wallet.feature.onboarding.impl.congratulations.CongratulationsController
import org.ton.wallet.feature.onboarding.impl.finished.OnboardingFinishedController
import org.ton.wallet.feature.onboarding.impl.importation.ImportController
import org.ton.wallet.feature.onboarding.impl.nophrase.NoPhraseController
import org.ton.wallet.feature.onboarding.impl.recovery.check.RecoveryCheckController
import org.ton.wallet.feature.onboarding.impl.recovery.finished.RecoveryFinishedController
import org.ton.wallet.feature.onboarding.impl.recovery.show.RecoveryShowController
import org.ton.wallet.feature.onboarding.impl.start.StartController
import org.ton.wallet.feature.passcode.impl.enter.PassCodeEnterController
import org.ton.wallet.feature.passcode.impl.setup.PassCodeSetupController
import org.ton.wallet.feature.scanqr.impl.ScanQrController
import org.ton.wallet.feature.send.impl.address.SendAddressController
import org.ton.wallet.feature.send.impl.amount.SendAmountController
import org.ton.wallet.feature.send.impl.confirm.SendConfirmController
import org.ton.wallet.feature.send.impl.connect.SendConnectConfirmController
import org.ton.wallet.feature.send.impl.processing.SendProcessingController
import org.ton.wallet.feature.settings.impl.SettingsController
import org.ton.wallet.feature.tonconnect.impl.TonConnectApproveController
import org.ton.wallet.feature.transactions.impl.TransactionDetailsController
import org.ton.wallet.feature.wallet.impl.main.MainScreenController
import org.ton.wallet.lib.log.L
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.controller.BaseBottomSheetController
import org.ton.wallet.screen.viewmodel.BaseViewModel
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarMessage
import org.ton.wallet.uikit.RUiKitDrawable
import java.lang.ref.WeakReference

class ControllerFactory {

    private var activityRef: WeakReference<Activity>? = null

    init {
        val snackBarController = Injector.appDiScope.getInstance<SnackBarController>()
        BaseViewModel.DefaultCoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            val messageText = AppErrorHandler.getErrorMessage(throwable)
            messageText?.let { msg ->
                val message = SnackBarMessage(
                    title = Res.str(RString.error),
                    message = msg,
                    drawable = Res.drawable(RUiKitDrawable.ic_warning_32)
                )
                snackBarController.showMessage(message)
            }
            L.e(throwable)
        }
        BaseViewModel.ErrorMessageProvider = AppErrorHandler::getErrorMessage
    }

    fun setActivity(activity: Activity) {
        activityRef = weak(activity)
    }

    fun getController(screen: String, args: Bundle?): Controller {
        return when (AppScreen.valueOf(screen)) {
            AppScreen.Main -> MainScreenController(args)
            AppScreen.OnboardingCongratulations -> CongratulationsController(args)
            AppScreen.OnboardingFinished -> OnboardingFinishedController(args)
            AppScreen.OnboardingImport -> ImportController(args)
            AppScreen.OnboardingNoPhrase -> NoPhraseController(args)
            AppScreen.OnboardingRecoveryCheck -> RecoveryCheckController(args)
            AppScreen.OnboardingRecoveryFinished -> RecoveryFinishedController(args)
            AppScreen.OnboardingRecoveryShow -> RecoveryShowController(args)
            AppScreen.OnboardingStart -> StartController(args)
            AppScreen.PassCodeEnter -> PassCodeEnterController(args)
            AppScreen.PassCodeSetup -> PassCodeSetupController(args)
            AppScreen.Receive -> ReceiveController(args)
            AppScreen.ScanQr -> ScanQrController(args)
            AppScreen.SendAddress -> SendAddressController(args)
            AppScreen.SendAmount -> SendAmountController(args)
            AppScreen.SendConfirm -> SendConfirmController(args)
            AppScreen.SendConnect -> SendConnectConfirmController(args)
            AppScreen.SendProcessing -> SendProcessingController(args)
            AppScreen.Settings -> SettingsController(args)
            AppScreen.TonConnectApprove -> TonConnectApproveController(args)
            AppScreen.TransactionDetails -> TransactionDetailsController(args)
        }
    }

    fun getDefaultPushChangeHandler(controller: Controller): ControllerChangeHandler {
        return if (controller is BaseBottomSheetController) {
            BottomSheetChangeHandler()
        } else {
            SlideChangeHandler.create(true)
        }
    }

    fun getDefaultPopChangeHandler(controller: Controller): ControllerChangeHandler {
        return if (controller is BaseBottomSheetController) {
            BottomSheetChangeHandler()
        } else {
            SlideChangeHandler.create(true)
        }
    }
}