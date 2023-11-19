plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
}

android {
    namespace = "org.ton.wallet.feature.tonconnect.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.coil)
    implementation(Config.Lib.conductor)
    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataWalletApi))
    implementation(project(Config.Module.domainTonConnectApi))
    implementation(project(Config.Module.domainWalletApi))
    implementation(project(Config.Module.featureTonConnectApi))
    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libCoreUi))
    implementation(project(Config.Module.libScreen))
    implementation(project(Config.Module.libTonConnect))
    implementation(project(Config.Module.strings))
    implementation(project(Config.Module.uicomponents))
    implementation(project(Config.Module.uikit))
}