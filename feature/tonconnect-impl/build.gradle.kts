plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id("kotlin-parcelize")
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.feature.tonconnect.impl"
}

dependencies {
    implementation(libs.coil)
    implementation(libs.conductor)
    implementation(project(":data:core"))
    implementation(project(":data:wallet-api"))
    implementation(project(":domain:tonconnect-api"))
    implementation(project(":domain:wallet-api"))
    implementation(project(":feature:tonconnect-api"))
    implementation(project(":lib:core"))
    implementation(project(":lib:core-ui"))
    implementation(project(":lib:screen"))
    implementation(project(":lib:tonconnect"))
    implementation(project(":strings"))
    implementation(project(":uicomponents"))
    implementation(project(":uikit"))
}