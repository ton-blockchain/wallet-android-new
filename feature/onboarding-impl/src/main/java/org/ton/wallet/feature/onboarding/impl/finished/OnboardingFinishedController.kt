package org.ton.wallet.feature.onboarding.impl.finished

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.view.isVisible
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.ext.setOnClickListenerWithLock
import org.ton.wallet.feature.onboarding.impl.R
import org.ton.wallet.rlottie.RLottieImageView
import org.ton.wallet.screen.controller.BaseViewModelController
import org.ton.wallet.screen.getScreenArguments
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.strings.RString
import org.ton.wallet.uikit.RUiKitRaw

class OnboardingFinishedController(args: Bundle?) : BaseViewModelController<OnboardingFinishedViewModel>(args)  {

    override val viewModel by viewModels { OnboardingFinishedViewModel(args.getScreenArguments()) }

    private lateinit var animationView: RLottieImageView

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_onboarding_finished, container, false)
        val doneButton = view.findViewById<TextView>(R.id.onboardingFinishedDoneButton)
        doneButton.setOnClickListenerWithLock(::onDoneClicked)

        animationView = view.findViewById(R.id.passCodeCompletedAnimationView)

        if (viewModel.isImport) {
            view.findViewById<TextView>(R.id.passCodeCompletedTitle).text = Res.str(RString.wallet_just_imported)
            view.findViewById<TextView>(R.id.passCodeCompletedSubtitle).isVisible = false
            doneButton.text = Res.str(RString.proceed)
            animationView.setLottieResource(RUiKitRaw.lottie_congratulations)
        } else {
            animationView.setLottieResource(RUiKitRaw.lottie_success)
        }

        return view
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)
        if (changeType.isEnter) {
            animationView.playAnimation()
        }
    }

    private fun onDoneClicked() {
        view?.let { v ->
            val screenShotBitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(screenShotBitmap)
            v.draw(canvas)
            canvas.setBitmap(null)
            viewModel.onDoneClicked(screenShotBitmap)
        }
    }
}