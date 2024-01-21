plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.feature.settings.impl"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.conductor)
    implementation(libs.easyPermissions)
    implementation(libs.recyclerview)

    implementation(project(":data:core"))
    implementation(project(":data:auth-api"))
    implementation(project(":data:notifications-api"))
    implementation(project(":data:settings-api"))
    implementation(project(":data:wallet-api"))
    implementation(project(":domain:settings-api"))
    implementation(project(":feature:passcode-api"))
    implementation(project(":feature:settings-api"))

    implementation(project(":lib:core"))
    implementation(project(":lib:core-ui"))
    implementation(project(":lib:lists"))
    implementation(project(":lib:screen"))
    implementation(project(":lib:security"))
    implementation(project(":strings"))
    implementation(project(":uicomponents"))
    implementation(project(":uikit"))
}