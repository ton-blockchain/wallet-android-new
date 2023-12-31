plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.domain.tonconnect.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.coroutines)
    implementation(Config.Lib.json)
    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataTonConnectApi))
    implementation(project(Config.Module.dataWalletApi))
    implementation(project(Config.Module.domainBlockchainApi))
    implementation(project(Config.Module.domainTonConnectApi))
    implementation(project(Config.Module.domainWalletApi))
    implementation(project(Config.Module.libCore))
}