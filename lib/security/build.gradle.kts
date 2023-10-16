plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.ton.wallet.lib.security"
    compileSdk = Config.Build.compileSdk
    ndkVersion = Config.Build.ndkVersion
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    kotlinOptions.jvmTarget = Config.Version.jvmTarget

    externalNativeBuild {
        cmake {
            path(file("src/main/cpp/CMakeLists.txt"))
        }
    }
}

dependencies {
    implementation(Config.Lib.biometric)
}