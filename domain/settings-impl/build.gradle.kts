plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.domain.settings.impl"
}

dependencies {
    implementation(project(":data:core"))
    implementation(project(":domain:settings-api"))
    implementation(project(":lib:sqlite"))
    implementation(project(":lib:tonconnect"))
}