plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
}

android {
    namespace = "org.ton.wallet.data.core"
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
    implementation(Config.Lib.annotation)
    implementation(Config.Lib.coroutines)
    implementation(platform(Config.Lib.okHttpBom))
    implementation(Config.Lib.okHttpCore)
    implementation(Config.Lib.serializationJson)
    implementation(Config.Lib.tonKotlin)

    implementation(project(Config.Module.libCore))
}