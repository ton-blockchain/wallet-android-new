plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.data.notifications.impl"
}

dependencies {
    implementation(libs.easyPermissions)
    implementation(project(":data:core"))
    implementation(project(":data:notifications-api"))
    implementation(project(":lib:core"))
    implementation(project(":strings"))
}