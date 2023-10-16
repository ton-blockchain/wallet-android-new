plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
}

android {
    namespace = "org.ton.wallet.feature.transactions.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.conductor)
    implementation(project(Config.Module.dataTransactionsApi))
    implementation(project(Config.Module.domainTransactionsApi))
    implementation(project(Config.Module.featureTransactionsApi))
    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libCoreUi))
    implementation(project(Config.Module.libLog))
    implementation(project(Config.Module.libRLottie))
    implementation(project(Config.Module.libScreen))
    implementation(project(Config.Module.strings))
    implementation(project(Config.Module.uicomponents))
    implementation(project(Config.Module.uikit))
}