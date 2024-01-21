plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id("kotlin-parcelize")
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.feature.transactions.impl"
}

dependencies {
    implementation(libs.conductor)
    implementation(libs.recyclerview)
    implementation(project(":data:core"))
    implementation(project(":data:transactions-api"))
    implementation(project(":domain:transactions-api"))
    implementation(project(":feature:transactions-api"))
    implementation(project(":lib:core"))
    implementation(project(":lib:core-ui"))
    implementation(project(":lib:log"))
    implementation(project(":lib:lists"))
    implementation(project(":lib:rlottie"))
    implementation(project(":lib:screen"))
    implementation(project(":strings"))
    implementation(project(":uicomponents"))
    implementation(project(":uikit"))
}