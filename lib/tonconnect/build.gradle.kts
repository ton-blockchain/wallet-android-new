plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlinx-serialization")
    id("kotlin-parcelize")
}

android {
    namespace = "org.ton.wallet.lib.tonconnect"
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
    implementation(Config.Lib.annotation)
    implementation(Config.Lib.coroutines)
    implementation(platform(Config.Lib.okHttpBom))
    implementation(Config.Lib.okHttpCore)
    implementation(Config.Lib.okHttpLogging)
    implementation(Config.Lib.okHttpSse)
    implementation(Config.Lib.serializationJson)

    implementation(project(Config.Module.dataCore))
    implementation(project(Config.Module.libCore))
    implementation(project(Config.Module.libLog))
}