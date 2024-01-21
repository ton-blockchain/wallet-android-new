plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.data.settings.impl"
}

dependencies {
    implementation(libs.coroutines)
    implementation(project(":data:core"))
    implementation(project(":data:settings-api"))
    implementation(project(":lib:log"))
}