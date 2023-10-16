plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
}

android {
    namespace = "org.ton.wallet.feature.onboarding.impl"
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
    implementation(Config.Lib.conductor)
    implementation(Config.Lib.recyclerView)

    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataAuthApi))
    implementation(project(Config.Module.dataSettingsApi))
    implementation(project(Config.Module.dataTonClientApi))
    implementation(project(Config.Module.dataWalletApi))
    implementation(project(Config.Module.domainBlockchainApi))
    implementation(project(Config.Module.domainWalletApi))
    implementation(project(Config.Module.featureOnboardingApi))
    implementation(project(Config.Module.featurePasscodeApi))

    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libCoreUi))
    implementation(project(Config.Module.libLists))
    implementation(project(Config.Module.libLog))
    implementation(project(Config.Module.libScreen))
    implementation(project(Config.Module.libSecurity))
    implementation(project(Config.Module.libRLottie))

    implementation(project(Config.Module.strings))
    implementation(project(Config.Module.uicomponents))
    implementation(project(Config.Module.uikit))
}