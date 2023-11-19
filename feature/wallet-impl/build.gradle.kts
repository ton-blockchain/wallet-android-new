plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.feature.wallet.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.conductor)
    implementation(Config.Lib.constraintLayout)
    implementation(Config.Lib.easyPermissions)
    implementation(Config.Lib.recyclerView)

    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataNotificationsApi))
    implementation(project(Config.Module.dataSettingsApi))
    implementation(project(Config.Module.dataTransactionsApi))
    implementation(project(Config.Module.dataWalletApi))
    implementation(project(Config.Module.domainTransactionsApi))
    implementation(project(Config.Module.domainWalletApi))
    implementation(project(Config.Module.featureScanQrApi))
    implementation(project(Config.Module.featureWalletApi))

    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libCoreUi))
    implementation(project(Config.Module.libDi))
    implementation(project(Config.Module.libLists))
    implementation(project(Config.Module.libLog))
    implementation(project(Config.Module.libQrBuilder))
    implementation(project(Config.Module.libRLottie))
    implementation(project(Config.Module.libScreen))

    implementation(project(Config.Module.strings))
    implementation(project(Config.Module.uicomponents))
    implementation(project(Config.Module.uikit))
}