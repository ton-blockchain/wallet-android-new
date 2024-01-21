plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.feature.wallet.impl"
}

dependencies {
    implementation(libs.conductor)
    implementation(libs.constraintLayout)
    implementation(libs.easyPermissions)
    implementation(libs.recyclerview)

    implementation(project(":data:core"))
    implementation(project(":data:notifications-api"))
    implementation(project(":data:settings-api"))
    implementation(project(":data:transactions-api"))
    implementation(project(":data:wallet-api"))
    implementation(project(":domain:transactions-api"))
    implementation(project(":domain:wallet-api"))
    implementation(project(":feature:scanqr-api"))
    implementation(project(":feature:wallet-api"))

    implementation(project(":lib:core"))
    implementation(project(":lib:core-ui"))
    implementation(project(":lib:di"))
    implementation(project(":lib:lists"))
    implementation(project(":lib:log"))
    implementation(project(":lib:qr-builder"))
    implementation(project(":lib:rlottie"))
    implementation(project(":lib:screen"))

    implementation(project(":strings"))
    implementation(project(":uicomponents"))
    implementation(project(":uikit"))
}