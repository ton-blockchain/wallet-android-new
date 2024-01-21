plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.data.notifications.api"
}

dependencies {
    implementation(libs.easyPermissions)
    implementation(project(":data:core"))
}