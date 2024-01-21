plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.data.tonclient.api"
}

dependencies {
    implementation(libs.tonKotlin)
    implementation(project(":data:tonapi"))
}