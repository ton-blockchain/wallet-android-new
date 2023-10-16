plugins {
    id("com.android.library")
}

android {
    namespace = "org.ton.wallet.lib.rlottie"
    compileSdk = Config.Build.compileSdk
    ndkVersion = Config.Build.ndkVersion
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    compileOptions {
        sourceCompatibility = Config.Version.javaSource
        targetCompatibility = Config.Version.javaTarget
    }

    externalNativeBuild {
        cmake {
            path(file("src/main/cpp/CMakeLists.txt"))
        }
    }
}