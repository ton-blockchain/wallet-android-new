plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    id("kotlin-parcelize")
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.app"

    defaultConfig {
        applicationId = "org.ton.wallet"
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
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
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(libs.appcompat)
    implementation(libs.biometric)
    implementation(libs.browser)
    implementation(libs.conductor)
    implementation(libs.coroutines)
    implementation(libs.easyPermissions)
    implementation(libs.serializationJson)
    implementation(platform(libs.okHttpBom))
    implementation(libs.okHttpCore)
    implementation(libs.okHttpLogging)
    implementation(libs.recyclerview)
    implementation(libs.security)

    implementation(project(":lib:core"))
    implementation(project(":lib:di"))
    implementation(project(":lib:lists"))
    implementation(project(":lib:log"))
    implementation(project(":lib:screen"))
    implementation(project(":lib:security"))
    implementation(project(":lib:sqlite"))
    implementation(project(":lib:tonconnect"))

    implementation(project(":data:auth-api"))
    implementation(project(":data:auth-impl"))
    implementation(project(":data:core"))
    implementation(project(":data:notifications-api"))
    implementation(project(":data:notifications-impl"))
    implementation(project(":data:prices-api"))
    implementation(project(":data:prices-impl"))
    implementation(project(":data:settings-api"))
    implementation(project(":data:settings-impl"))
    implementation(project(":data:transactions-api"))
    implementation(project(":data:transactions-impl"))
    implementation(project(":data:tonclient-api"))
    implementation(project(":data:tonclient-impl"))
    implementation(project(":data:wallet-api"))
    implementation(project(":data:wallet-impl"))

    implementation(project(":domain:blockchain-api"))
    implementation(project(":domain:blockchain-impl"))
    implementation(project(":domain:settings-api"))
    implementation(project(":domain:settings-impl"))
    implementation(project(":domain:tonconnect-api"))
    implementation(project(":domain:tonconnect-impl"))
    implementation(project(":domain:transactions-api"))
    implementation(project(":domain:transactions-impl"))
    implementation(project(":domain:wallet-api"))
    implementation(project(":domain:wallet-impl"))

    implementation(project(":feature:onboarding-api"))
    implementation(project(":feature:onboarding-impl"))
    implementation(project(":feature:passcode-api"))
    implementation(project(":feature:passcode-impl"))
    implementation(project(":feature:scanqr-api"))
    implementation(project(":feature:scanqr-impl"))
    implementation(project(":feature:send-api"))
    implementation(project(":feature:send-impl"))
    implementation(project(":feature:settings-api"))
    implementation(project(":feature:settings-impl"))
    implementation(project(":feature:tonconnect-api"))
    implementation(project(":feature:tonconnect-impl"))
    implementation(project(":feature:transactions-api"))
    implementation(project(":feature:transactions-impl"))
    implementation(project(":feature:wallet-api"))
    implementation(project(":feature:wallet-impl"))

    implementation(project(":strings"))
    implementation(project(":uicomponents"))
    implementation(project(":uikit"))
}