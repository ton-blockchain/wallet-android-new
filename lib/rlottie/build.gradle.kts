plugins {
    alias(libs.plugins.androidLibrary)
}

apply(from = "${rootDir}/gradle/common-java.gradle")

android {
    namespace = "org.ton.wallet.lib.rlottie"

    externalNativeBuild {
        cmake {
            path(file("src/main/cpp/CMakeLists.txt"))
        }
    }
}