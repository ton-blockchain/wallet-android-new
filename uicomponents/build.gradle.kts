plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.uicomponents"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.activity)
    implementation(Config.Lib.appCompat)
    implementation(Config.Lib.core)
    implementation(Config.Lib.recyclerView)

    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libCoreUi))
    implementation(project(Config.Module.libLists))
    implementation(project(Config.Module.libLog))
    implementation(project(Config.Module.libRLottie))

    implementation(project(Config.Module.strings))
    implementation(project(Config.Module.uikit))
}