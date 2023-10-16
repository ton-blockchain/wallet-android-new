plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.domain.settings.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.domainSettingsApi))
    implementation(project(Config.Module.libSqlite))
}