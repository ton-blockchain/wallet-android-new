plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.data.prices.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.coroutines)
    implementation(platform(Config.Lib.okHttpBom))
    implementation(Config.Lib.okHttpCore)

    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataPricesApi))
    implementation(project(Config.Module.libLog))
    implementation(project(Config.Module.libSqlite))
}