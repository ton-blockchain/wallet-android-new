plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.data.tonconnect.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.coroutines)
    implementation(Config.Lib.json)
    implementation(platform(Config.Lib.okHttpBom))
    implementation(Config.Lib.okHttpCore)
    implementation(Config.Lib.okHttpSse)

    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataTonConnectApi))
    implementation(project(Config.Module.dataWalletApi))
    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libLog))
    implementation(project(Config.Module.libSecurity))
    implementation(project(Config.Module.libSqlite))
}