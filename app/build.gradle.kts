plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
}

android {
    namespace = "org.ton.wallet.app"
    compileSdk = Config.Build.compileSdk

    defaultConfig {
        applicationId = "org.ton.wallet"
        minSdk = Config.Build.minSdk
        targetSdk = Config.Build.targetSdk
        versionCode = Config.App.versionCode
        versionName = Config.App.versionName
    }
    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("./config/release.keystore")
            storePassword = "${System.getenv()["RELEASE_STORE_PASSWORD"]}"
            keyAlias = "${System.getenv()["RELEASE_KEY_ALIAS"]}"
            keyPassword = "${System.getenv()["RELEASE_KEY_PASSWORD"]}"
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isDebuggable = false
            isJniDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    bundle {
        language {
            enableSplit = false
        }
        abi {
            enableSplit = true
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = Config.Version.javaSource
        targetCompatibility = Config.Version.javaTarget
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget
}

dependencies {
    coreLibraryDesugaring(Config.Lib.desugar)

    implementation(Config.Lib.appCompat)
    implementation(Config.Lib.biometric)
    implementation(Config.Lib.browser)
    implementation(Config.Lib.conductor)
    implementation(Config.Lib.easyPermissions)
    implementation(Config.Lib.json)
    implementation(platform(Config.Lib.okHttpBom))
    implementation(Config.Lib.okHttpCore)
    implementation(Config.Lib.okHttpLogging)
    implementation(Config.Lib.recyclerView)
    implementation(Config.Lib.security)

    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libDi))
    implementation(project(Config.Module.libLists))
    implementation(project(Config.Module.libLog))
    implementation(project(Config.Module.libScreen))
    implementation(project(Config.Module.libSecurity))
    implementation(project(Config.Module.libSqlite))
    implementation(project(Config.Module.libTonConnect))

    implementation(project(Config.Module.dataAuthApi))
    implementation(project(Config.Module.dataAuthImpl))
    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.dataNotificationsApi))
    implementation(project(Config.Module.dataNotificationsImpl))
    implementation(project(Config.Module.dataPricesApi))
    implementation(project(Config.Module.dataPricesImpl))
    implementation(project(Config.Module.dataSettingsApi))
    implementation(project(Config.Module.dataSettingsImpl))
    implementation(project(Config.Module.dataTransactionsApi))
    implementation(project(Config.Module.dataTransactionsImpl))
    implementation(project(Config.Module.dataTonClientApi))
    implementation(project(Config.Module.dataTonClientImpl))
    implementation(project(Config.Module.dataWalletApi))
    implementation(project(Config.Module.dataWalletImpl))

    implementation(project(Config.Module.domainBlockchainApi))
    implementation(project(Config.Module.domainBlockchainImpl))
    implementation(project(Config.Module.domainSettingsApi))
    implementation(project(Config.Module.domainSettingsImpl))
    implementation(project(Config.Module.domainTonConnectApi))
    implementation(project(Config.Module.domainTonConnectImpl))
    implementation(project(Config.Module.domainTransactionsApi))
    implementation(project(Config.Module.domainTransactionsImpl))
    implementation(project(Config.Module.domainWalletApi))
    implementation(project(Config.Module.domainWalletImpl))

    implementation(project(Config.Module.featureOnboardingApi))
    implementation(project(Config.Module.featureOnboardingImpl))
    implementation(project(Config.Module.featurePasscodeApi))
    implementation(project(Config.Module.featurePasscodeImpl))
    implementation(project(Config.Module.featureScanQrApi))
    implementation(project(Config.Module.featureScanQrImpl))
    implementation(project(Config.Module.featureSendApi))
    implementation(project(Config.Module.featureSendImpl))
    implementation(project(Config.Module.featureSettingsApi))
    implementation(project(Config.Module.featureSettingsImpl))
    implementation(project(Config.Module.featureTonConnectApi))
    implementation(project(Config.Module.featureTonConnectImpl))
    implementation(project(Config.Module.featureTransactionsApi))
    implementation(project(Config.Module.featureTransactionsImpl))
    implementation(project(Config.Module.featureWalletApi))
    implementation(project(Config.Module.featureWalletImpl))

    implementation(project(Config.Module.strings))
    implementation(project(Config.Module.uicomponents))
    implementation(project(Config.Module.uikit))
}