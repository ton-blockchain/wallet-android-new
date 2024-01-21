plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.data.auth.impl"
}

dependencies {
    implementation(libs.coroutines)
    implementation(project(":data:auth-api"))
    implementation(project(":data:core"))
    implementation(project(":lib:core"))
    implementation(project(":lib:security"))
}