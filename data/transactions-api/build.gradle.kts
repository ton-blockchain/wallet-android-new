plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.data.transactions.api"
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.tonKotlin)
    implementation(project(":data:core"))
    implementation(project(":data:wallet-api"))
    implementation(project(":lib:core"))
}