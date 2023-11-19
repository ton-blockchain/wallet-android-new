plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.data.transactions.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    buildFeatures {
        buildConfig = true
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.core)
    implementation(Config.Lib.coroutines)
    implementation(Config.Lib.tonKotlin)
    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataTonApi))
    implementation(project(Config.Module.dataTonClientApi))
    implementation(project(Config.Module.dataTransactionsApi))
    implementation(project(Config.Module.dataWalletApi))
    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libLog))
    implementation(project(Config.Module.libSqlite))
}