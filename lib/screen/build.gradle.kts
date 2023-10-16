plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.lib.controller"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.conductor)
    implementation(Config.Lib.core)
    implementation(Config.Lib.coroutines)
    implementation(Config.Lib.easyPermissions)
    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libCoreUi))
    implementation(project(Config.Module.libDi))
    implementation(project(Config.Module.uikit))
}