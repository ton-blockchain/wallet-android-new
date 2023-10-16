plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.domain.blockhain.impl"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataTonApi))
    implementation(project(Config.Module.dataTonClientApi))
    implementation(project(Config.Module.dataWalletApi))
    implementation(project(Config.Module.domainBlockchainApi))
    implementation(project(Config.Module.libCore))
}