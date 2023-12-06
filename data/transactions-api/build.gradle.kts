plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlinx-serialization")
}

android {
    namespace = "org.ton.wallet.data.transactions.api"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.coroutines)
    implementation(Config.Lib.tonKotlin)
    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataWalletApi))
    implementation(project(Config.Module.libCore))
}