plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.lib.core"
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.serializationCore)
}