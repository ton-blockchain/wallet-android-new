plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.domain.blockhain.impl"
}

dependencies {
    implementation(project(":data:core"))
    implementation(project(":data:tonapi"))
    implementation(project(":data:tonclient-api"))
    implementation(project(":data:wallet-api"))
    implementation(project(":domain:blockchain-api"))
    implementation(project(":lib:core"))
}