import org.gradle.api.JavaVersion

object Config {

    object App {

        const val versionCode = 1
        const val versionName = "1.0.0"
    }

    object Build {

        const val compileSdk = 34
        const val minSdk = 23
        const val ndkVersion = "21.4.7075529"
        const val targetSdk = 34
    }

    object Version {

        const val kotlin = "1.9.0"
        const val jvmTarget = "1.8"
        val javaSource = JavaVersion.VERSION_1_8
        val javaTarget = JavaVersion.VERSION_1_8
    }

    object Lib {

        private const val CameraVersion = "1.2.3"

        const val activity = "androidx.activity:activity:1.7.1"
        const val annotation = "androidx.annotation:annotation:1.6.0"
        const val appCompat = "androidx.appcompat:appcompat:1.6.1"
        const val barCodeScanner = "com.google.mlkit:barcode-scanning:17.2.0"
        const val biometric = "androidx.biometric:biometric:1.1.0"
        const val browser = "androidx.browser:browser:1.6.0"
        const val camera2 = "androidx.camera:camera-camera2:${CameraVersion}"
        const val cameraCore = "androidx.camera:camera-core:${CameraVersion}"
        const val cameraLifecycle = "androidx.camera:camera-lifecycle:${CameraVersion}"
        const val cameraView = "androidx.camera:camera-view:${CameraVersion}"
        const val coil = "io.coil-kt:coil:2.4.0"
        const val conductor = "com.bluelinelabs:conductor:4.0.0-preview-3"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.4"
        const val core = "androidx.core:core-ktx:1.10.1"
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
        const val desugar = "com.android.tools:desugar_jdk_libs:2.0.3"
        const val easyPermissions = "pub.devrel:easypermissions:3.0.0"
        const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0"
        const val okHttpBom = "com.squareup.okhttp3:okhttp-bom:4.11.0"
        const val okHttpCore = "com.squareup.okhttp3:okhttp"
        const val okHttpLogging = "com.squareup.okhttp3:logging-interceptor"
        const val okHttpSse = "com.squareup.okhttp3:okhttp-sse"
        const val recyclerView = "androidx.recyclerview:recyclerview:1.3.1"
        const val security = "androidx.security:security-crypto:1.0.0"
        const val tonKotlin = "org.ton:ton-kotlin:0.2.15"
    }

    object Module {

        const val libCore = ":lib:core"
        const val libCoreUi = ":lib:core-ui"
        const val libDi = ":lib:di"
        const val libLists = ":lib:lists"
        const val libLog = ":lib:log"
        const val libQrBuilder = ":lib:qr-builder"
        const val libRLottie = ":lib:rlottie"
        const val libScreen = ":lib:screen"
        const val libSecurity = ":lib:security"
        const val libSqlite = ":lib:sqlite"
        const val libSqliteLib = ":lib:sqlite-lib"

        const val dataAuthApi = ":data:auth-api"
        const val dataAuthImpl = ":data:auth-impl"
        const val dataCore = ":data:core"
        const val dataNotificationsApi = ":data:notifications-api"
        const val dataNotificationsImpl = ":data:notifications-impl"
        const val dataPricesApi = ":data:prices-api"
        const val dataPricesImpl = ":data:prices-impl"
        const val dataSettingsApi = ":data:settings-api"
        const val dataSettingsImpl = ":data:settings-impl"
        const val dataTonApi = ":data:tonapi"
        const val dataTonClientApi = ":data:tonclient-api"
        const val dataTonClientImpl = ":data:tonclient-impl"
        const val dataTonConnectApi = ":data:tonconnect-api"
        const val dataTonConnectImpl = ":data:tonconnect-impl"
        const val dataTransactionsApi = ":data:transactions-api"
        const val dataTransactionsImpl = ":data:transactions-impl"
        const val dataWalletApi = ":data:wallet-api"
        const val dataWalletImpl = ":data:wallet-impl"

        const val domainBlockchainApi = ":domain:blockchain-api"
        const val domainBlockchainImpl = ":domain:blockchain-impl"
        const val domainSettingsApi = ":domain:settings-api"
        const val domainSettingsImpl = ":domain:settings-impl"
        const val domainTonConnectApi = ":domain:tonconnect-api"
        const val domainTonConnectImpl = ":domain:tonconnect-impl"
        const val domainTransactionsApi = ":domain:transactions-api"
        const val domainTransactionsImpl = ":domain:transactions-impl"
        const val domainWalletApi = ":domain:wallet-api"
        const val domainWalletImpl = ":domain:wallet-impl"

        const val featureOnboardingApi = ":feature:onboarding-api"
        const val featureOnboardingImpl = ":feature:onboarding-impl"
        const val featurePasscodeApi = ":feature:passcode-api"
        const val featurePasscodeImpl = ":feature:passcode-impl"
        const val featureScanQrApi = ":feature:scanqr-api"
        const val featureScanQrImpl = ":feature:scanqr-impl"
        const val featureSendApi = ":feature:send-api"
        const val featureSendImpl = ":feature:send-impl"
        const val featureSettingsApi = ":feature:settings-api"
        const val featureSettingsImpl = ":feature:settings-impl"
        const val featureTonConnectApi = ":feature:tonconnect-api"
        const val featureTonConnectImpl = ":feature:tonconnect-impl"
        const val featureTransactionsApi = ":feature:transactions-api"
        const val featureTransactionsImpl = ":feature:transactions-impl"
        const val featureWalletApi = ":feature:wallet-api"
        const val featureWalletImpl = ":feature:wallet-impl"

        const val strings = ":strings"
        const val uicomponents = ":uicomponents"
        const val uikit = ":uikit"
    }
}