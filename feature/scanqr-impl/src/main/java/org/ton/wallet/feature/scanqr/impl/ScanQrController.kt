package org.ton.wallet.feature.scanqr.impl

import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.core.view.*
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.ext.setOnClickListener
import org.ton.wallet.data.core.util.CoroutineScopes
import org.ton.wallet.lib.log.L
import org.ton.wallet.screen.controller.BaseViewModelController
import org.ton.wallet.screen.getScreenArguments
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarMessage
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.util.concurrent.*

class ScanQrController(args: Bundle?) : BaseViewModelController<ScanQrViewModel>(args) {

    override val viewModel by viewModels { ScanQrViewModel(args.getScreenArguments()) }
    private val snackBarController: SnackBarController by inject()

    override val useTopInsetsPadding: Boolean = false
    override val useBottomInsetsPadding: Boolean = false

    private val qrForeground = ScanQrForegroundDrawable()
    private val cameraFutureProvider: ListenableFuture<ProcessCameraProvider> by lazy {
        ProcessCameraProvider.getInstance(activity!!)
    }

    private lateinit var qrCodeAnalyzer: QrCodeAnalyzer
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var mainExecutor: Executor
    private lateinit var backButton: View
    private lateinit var noPermissionsGroup: Group
    private lateinit var cameraGroup: Group
    private lateinit var previewView: PreviewView
    private lateinit var previewForegroundView: View
    private lateinit var titleText: TextView

    private var initialBackButtonTopMargin = 0
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null

    private val hasCameraPermission: Boolean
        get() = activity?.let { act -> EasyPermissions.hasPermissions(act, android.Manifest.permission.CAMERA) } ?: false

    override fun onPreCreateView() {
        super.onPreCreateView()
        cameraExecutor = Executors.newSingleThreadExecutor()
        mainExecutor = ContextCompat.getMainExecutor(Res.context)
        qrCodeAnalyzer = QrCodeAnalyzer()
        qrCodeAnalyzer.setCallback(qrCallback)
        qrForeground.setCutoutSize(Res.dp(284))
        qrForeground.setFocusChangeListener(focusRectChangedListener)
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_scan_qr, container, false)
        view.findViewById<View>(R.id.scanQrOpenSettingsButton).setOnClickListener { viewModel.onOpenSettingsClicked(activity!!) }
        view.findViewById<View>(R.id.scanQrImageButton).setOnClickListener { viewModel.onImageClicked(activity!!) }
        view.findViewById<View>(R.id.scanQrFlashlightButton).setOnClickListener(::onFlashClicked)

        backButton = view.findViewById(R.id.scanQrBackButton)
        backButton.setOnClickListener(viewModel::onBackClicked)
        initialBackButtonTopMargin = (backButton.layoutParams as MarginLayoutParams).topMargin

        noPermissionsGroup = view.findViewById(R.id.scanQrNoPermissionGroup)
        cameraGroup = view.findViewById(R.id.scanQrCameraGroup)
        previewView = view.findViewById(R.id.scanQrPreviewView)
        previewForegroundView = view.findViewById(R.id.scanQrForegroundView)

        titleText = view.findViewById(R.id.scanQrTitle)

        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        checkPermissions()
    }

    override fun onDestroyView(view: View) {
        imageAnalysis?.clearAnalyzer()
        cameraProvider?.unbindAll()
        super.onDestroyView(view)
    }

    override fun onDestroy() {
        qrCodeAnalyzer.destroy()
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        val superInsets = super.onApplyWindowInsets(v, insets)
        val systemBarsInsets = superInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        backButton.updateLayoutParams<MarginLayoutParams> { topMargin = systemBarsInsets.top + initialBackButtonTopMargin }
        return superInsets
    }

    override fun onPermissionsGranted(requestCode: Int, permissions: MutableList<String>) {
        super.onPermissionsGranted(requestCode, permissions)
        if (requestCode == RequestCodeCameraPermission) {
            onPermissionChanged(hasCameraPermission)
            cameraFutureProvider.addListener(cameraProviderCallback, mainExecutor)
        }
    }

    override fun onPermissionsDenied(requestCode: Int, permissions: MutableList<String>) {
        super.onPermissionsDenied(requestCode, permissions)
        if (requestCode == RequestCodeCameraPermission) {
            onPermissionChanged(hasCameraPermission)
        }
    }

    override fun onActivityResultCallback(result: Any?) {
        super.onActivityResultCallback(result)
        if (result is Uri) {
            CoroutineScopes.appScope.launch(Dispatchers.Default) {
                val inputImage = InputImage.fromFilePath(Res.context, result)
                val value = qrCodeAnalyzer.getQrCodeValue(inputImage)
                if (value == null) {
                    snackBarController.showMessage(SnackBarMessage(title = Res.str(RString.no_qr_codes_detected)))
                } else {
                    viewModel.onQrDetected(value)
                }
            }
        }
    }

    private fun checkPermissions() {
        val activity = activity ?: return
        onPermissionChanged(hasCameraPermission)
        if (hasCameraPermission) {
            cameraFutureProvider.addListener(cameraProviderCallback, mainExecutor)
        } else {
            val request = PermissionRequest.Builder(activity, RequestCodeCameraPermission, android.Manifest.permission.CAMERA)
                .setRationale(Res.str(RString.no_camera_access_description))
                .setPositiveButtonText(Res.str(RString.ok))
                .setNegativeButtonText(Res.str(RString.cancel))
                .build()
            EasyPermissions.requestPermissions(request)
        }
    }

    private fun onPermissionChanged(hasCameraPermission: Boolean) {
        noPermissionsGroup.isVisible = !hasCameraPermission
        cameraGroup.isVisible = hasCameraPermission
        previewForegroundView.foreground =
            if (hasCameraPermission) qrForeground
            else null
        setNavigationBarLight(!hasCameraPermission)
        setStatusBarLight(!hasCameraPermission)
    }

    private fun onFlashClicked() {
        val camera = camera ?: return
        val isEnabled = camera.cameraInfo.torchState.value == TorchState.ON
        camera.cameraControl.enableTorch(!isEnabled)
    }

    private val cameraProviderCallback = Runnable {
        val cameraProvider = cameraFutureProvider.get()
        val preview = Preview.Builder()
            .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
            .build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
            .setImageQueueDepth(10)
            .build()
        imageAnalysis!!.setAnalyzer(cameraExecutor, qrCodeAnalyzer)

        cameraProvider.unbindAll()
        try {
            camera = cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
        } catch (e: Exception) {
            L.e(e)
        }
        this.cameraProvider = cameraProvider
    }

    private val qrCallback = object : QrCodeAnalyzer.QrCodeCallback {

        private var lastCallbackTimestampMs = 0L

        override fun onQrCodeDetected(value: String) {
            val currentTimestamp = SystemClock.elapsedRealtime()
            if (currentTimestamp - lastCallbackTimestampMs >= CheckQrDelayMs) {
                lastCallbackTimestampMs = currentTimestamp
                val isQrHandled = viewModel.onQrDetected(value)
                if (isQrHandled) {
                    imageAnalysis?.clearAnalyzer()
                }
            }
        }
    }

    private val focusRectChangedListener = ScanQrForegroundDrawable.OnFocusRectChangeListener { rect ->
        titleText.translationY = rect.top - titleText.height - Res.dp(28)
    }

    private companion object {

        private const val RequestCodeCameraPermission = 1
        private const val CheckQrDelayMs = 500L
    }
}