plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.data.wallet.impl"
}

dependencies {
    implementation(libs.core)
    implementation(libs.coroutines)
    implementation(libs.tonKotlin)

    implementation(project(":data:core"))
    implementation(project(":data:tonapi"))
    implementation(project(":data:tonclient-api"))
    implementation(project(":data:wallet-api"))
    implementation(project(":lib:core"))
    implementation(project(":lib:log"))
    implementation(project(":lib:security"))
    implementation(project(":lib:sqlite"))
}