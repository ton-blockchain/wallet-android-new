plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.domain.tonconnect.impl"
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.serializationJson)
    implementation(libs.tonKotlin)
    implementation(project(":data:core"))
    implementation(project(":data:wallet-api"))
    implementation(project(":domain:blockchain-api"))
    implementation(project(":domain:tonconnect-api"))
    implementation(project(":domain:wallet-api"))
    implementation(project(":lib:core"))
    implementation(project(":lib:security"))
    implementation(project(":lib:tonconnect"))
}