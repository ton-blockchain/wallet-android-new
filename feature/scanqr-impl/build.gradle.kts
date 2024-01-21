plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id("kotlin-parcelize")
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.feature.scanqr.impl"
}

dependencies {
    implementation(libs.barCodeScanner)
    implementation(libs.camera2)
    implementation(libs.cameraCore)
    implementation(libs.cameraLifecycle)
    implementation(libs.cameraView)
    implementation(libs.conductor)
    implementation(libs.constraintLayout)
    implementation(libs.easyPermissions)
    implementation(project(":data:core"))
    implementation(project(":feature:scanqr-api"))
    implementation(project(":lib:core"))
    implementation(project(":lib:core-ui"))
    implementation(project(":lib:log"))
    implementation(project(":lib:screen"))
    implementation(project(":strings"))
    implementation(project(":uicomponents"))
    implementation(project(":uikit"))
}