plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id("kotlin-parcelize")
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.feature.passcode.impl"
}

dependencies {
    implementation(libs.activity)
    implementation(libs.conductor)
    implementation(libs.core)
    implementation(libs.coroutines)

    implementation(project(":lib:core"))
    implementation(project(":lib:core-ui"))
    implementation(project(":lib:rlottie"))
    implementation(project(":lib:screen"))

    implementation(project(":data:auth-api"))
    implementation(project(":data:core"))
    implementation(project(":feature:passcode-api"))

    implementation(project(":strings"))
    implementation(project(":uicomponents"))
    implementation(project(":uikit"))
}