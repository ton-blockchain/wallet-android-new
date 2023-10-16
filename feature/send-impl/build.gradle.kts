plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
}

android {
    namespace = "org.ton.wallet.feature.send.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.conductor)
    implementation(Config.Lib.constraintLayout)
    implementation(Config.Lib.recyclerView)
    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataTonClientApi))
    implementation(project(Config.Module.dataTonConnectApi))
    implementation(project(Config.Module.dataTransactionsApi))
    implementation(project(Config.Module.domainBlockchainApi))
    implementation(project(Config.Module.domainTonConnectApi))
    implementation(project(Config.Module.domainTransactionsApi))
    implementation(project(Config.Module.domainWalletApi))
    implementation(project(Config.Module.featurePasscodeApi))
    implementation(project(Config.Module.featureScanQrApi))
    implementation(project(Config.Module.featureSendApi))
    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libCoreUi))
    implementation(project(Config.Module.libLists))
    implementation(project(Config.Module.libRLottie))
    implementation(project(Config.Module.libScreen))
    implementation(project(Config.Module.strings))
    implementation(project(Config.Module.uicomponents))
    implementation(project(Config.Module.uikit))
}