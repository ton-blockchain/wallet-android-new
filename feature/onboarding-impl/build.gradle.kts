plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id("kotlin-parcelize")
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.feature.onboarding.impl"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.conductor)
    implementation(libs.recyclerview)

    implementation(project(":data:core"))
    implementation(project(":data:auth-api"))
    implementation(project(":data:settings-api"))
    implementation(project(":data:tonclient-api"))
    implementation(project(":data:wallet-api"))
    implementation(project(":domain:blockchain-api"))
    implementation(project(":domain:wallet-api"))
    implementation(project(":feature:onboarding-api"))
    implementation(project(":feature:passcode-api"))

    implementation(project(":lib:core"))
    implementation(project(":lib:core-ui"))
    implementation(project(":lib:lists"))
    implementation(project(":lib:log"))
    implementation(project(":lib:screen"))
    implementation(project(":lib:security"))
    implementation(project(":lib:rlottie"))

    implementation(project(":strings"))
    implementation(project(":uicomponents"))
    implementation(project(":uikit"))
}