plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.data.notifications.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(Config.Lib.easyPermissions)
    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataNotificationsApi))
    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.strings))
}