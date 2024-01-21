plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.lib.sqlite"
}

dependencies {
    implementation(libs.appcompat)
    api(project(":lib:sqlite-lib", configuration = "default"))
}