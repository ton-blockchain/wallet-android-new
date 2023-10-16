plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.domain.transactions.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.coroutines)
    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataSettingsApi))
    implementation(project(Config.Module.dataTonClientApi))
    implementation(project(Config.Module.dataTransactionsApi))
    implementation(project(Config.Module.dataWalletApi))
    implementation(project(Config.Module.domainTransactionsApi))
    implementation(project(Config.Module.domainWalletApi))
    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libCoreUi))
    implementation(project(Config.Module.strings))
    implementation(project(Config.Module.uikit))
}