plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")
android {
    namespace = "org.ton.wallet.domain.transactions.impl"
}

dependencies {
    implementation(libs.coroutines)
    implementation(project(":data:core"))
    implementation(project(":data:settings-api"))
    implementation(project(":data:tonclient-api"))
    implementation(project(":data:transactions-api"))
    implementation(project(":data:wallet-api"))
    implementation(project(":domain:blockchain-api"))
    implementation(project(":domain:transactions-api"))
    implementation(project(":domain:wallet-api"))
    implementation(project(":lib:core"))
    implementation(project(":lib:core-ui"))
    implementation(project(":strings"))
    implementation(project(":uikit"))
}