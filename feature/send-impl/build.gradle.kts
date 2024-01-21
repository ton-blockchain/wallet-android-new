plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id("kotlin-parcelize")
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.feature.send.impl"
}

dependencies {
    implementation(libs.conductor)
    implementation(libs.constraintLayout)
    implementation(libs.recyclerview)
    implementation(libs.tonKotlin)
    implementation(project(":data:core"))
    implementation(project(":data:tonclient-api"))
    implementation(project(":data:transactions-api"))
    implementation(project(":data:wallet-api"))
    implementation(project(":domain:blockchain-api"))
    implementation(project(":domain:tonconnect-api"))
    implementation(project(":domain:transactions-api"))
    implementation(project(":domain:wallet-api"))
    implementation(project(":feature:passcode-api"))
    implementation(project(":feature:scanqr-api"))
    implementation(project(":feature:send-api"))
    implementation(project(":lib:core"))
    implementation(project(":lib:core-ui"))
    implementation(project(":lib:lists"))
    implementation(project(":lib:log"))
    implementation(project(":lib:rlottie"))
    implementation(project(":lib:screen"))
    implementation(project(":lib:tonconnect"))
    implementation(project(":strings"))
    implementation(project(":uicomponents"))
    implementation(project(":uikit"))
}