plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    id("kotlin-parcelize")
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.lib.tonconnect"

    externalNativeBuild {
        cmake {
            path(file("src/main/cpp/CMakeLists.txt"))
        }
    }
}

dependencies {
    implementation(libs.annotation)
    implementation(libs.coroutines)
    implementation(platform(libs.okHttpBom))
    implementation(libs.okHttpCore)
    implementation(libs.okHttpLogging)
    implementation(libs.okHttpSse)
    implementation(libs.serializationJson)

    implementation(project(":data:core"))
    implementation(project(":lib:core"))
    implementation(project(":lib:log"))
}