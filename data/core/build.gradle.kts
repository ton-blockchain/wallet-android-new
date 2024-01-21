plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    id("kotlin-parcelize")
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.data.core"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.annotation)
    implementation(libs.coroutines)
    implementation(platform(libs.okHttpBom))
    implementation(libs.okHttpCore)
    implementation(libs.serializationJson)
    implementation(libs.tonKotlin)

    implementation(project(":lib:core"))
}