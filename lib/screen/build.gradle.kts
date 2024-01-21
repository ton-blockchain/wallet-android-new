plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.lib.controller"
}

dependencies {
    implementation(libs.conductor)
    implementation(libs.core)
    implementation(libs.coroutines)
    implementation(libs.easyPermissions)
    implementation(project(":lib:core"))
    implementation(project(":lib:core-ui"))
    implementation(project(":lib:di"))
    implementation(project(":uikit"))
}