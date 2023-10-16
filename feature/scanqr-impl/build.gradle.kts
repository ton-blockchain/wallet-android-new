plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
}

android {
    namespace = "org.ton.wallet.feature.scanqr.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.barCodeScanner)
    implementation(Config.Lib.camera2)
    implementation(Config.Lib.cameraCore)
    implementation(Config.Lib.cameraLifecycle)
    implementation(Config.Lib.cameraView)
    implementation(Config.Lib.conductor)
    implementation(Config.Lib.constraintLayout)
    implementation(Config.Lib.easyPermissions)
    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.featureScanQrApi))
    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libCoreUi))
    implementation(project(Config.Module.libLog))
    implementation(project(Config.Module.libScreen))
    implementation(project(Config.Module.strings))
    implementation(project(Config.Module.uicomponents))
    implementation(project(Config.Module.uikit))
}