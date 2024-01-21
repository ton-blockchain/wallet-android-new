plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.domain.wallet.impl"
}

dependencies {
    implementation(libs.coroutines)
    implementation(project(":data:core"))
    implementation(project(":data:prices-api"))
    implementation(project(":data:settings-api"))
    implementation(project(":data:tonapi"))
    implementation(project(":data:tonclient-api"))
    implementation(project(":data:wallet-api"))
    implementation(project(":domain:wallet-api"))
    implementation(project(":lib:core"))
    implementation(project(":lib:core-ui"))
}