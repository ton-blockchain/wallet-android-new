plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
}

android {
    namespace = "org.ton.wallet.feature.passcode.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.activity)
    implementation(Config.Lib.conductor)
    implementation(Config.Lib.core)
    implementation(Config.Lib.coroutines)

    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libCoreUi))
    implementation(project(Config.Module.libRLottie))
    implementation(project(Config.Module.libScreen))

    implementation(project(Config.Module.dataAuthApi))
    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.featurePasscodeApi))

    implementation(project(Config.Module.strings))
    implementation(project(Config.Module.uicomponents))
    implementation(project(Config.Module.uikit))
}