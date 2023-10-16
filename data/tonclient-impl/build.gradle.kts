plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.data.tonclient.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.coroutines)
    implementation(Config.Lib.json)
    implementation(Config.Lib.tonKotlin)
    implementation(platform(Config.Lib.okHttpBom))
    implementation(Config.Lib.okHttpCore)

    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataTonApi))
    implementation(project(Config.Module.dataTonClientApi))
    implementation(project(Config.Module.libLog))
}