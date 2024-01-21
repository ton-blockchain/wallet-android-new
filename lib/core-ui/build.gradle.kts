plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.lib.coreui"
}

dependencies {
    implementation(libs.core)
    implementation(project(":lib:core"))
    implementation(project(":lib:log"))
}