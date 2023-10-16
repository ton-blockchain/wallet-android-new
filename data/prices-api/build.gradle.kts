plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.data.prices.api"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.coroutines)
    implementation(project(Config.Module.dataCore))
}