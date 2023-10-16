plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.lib.sqlite"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.appCompat)
    api(project(Config.Module.libSqliteLib, configuration = "default"))
}