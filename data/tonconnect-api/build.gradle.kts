plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
}

android {
    namespace = "org.ton.wallet.data.tonconnect.api"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.annotation)
    implementation(Config.Lib.coroutines)
    implementation(Config.Lib.json)
    implementation(project(Config.Module.dataCore))
}