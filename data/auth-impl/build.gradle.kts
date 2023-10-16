plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.data.auth.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.coroutines)
    implementation(project(Config.Module.dataAuthApi))
    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libSecurity))
}