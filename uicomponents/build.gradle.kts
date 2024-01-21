plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.uicomponents"
}

dependencies {
    implementation(libs.activity)
    implementation(libs.appcompat)
    implementation(libs.core)
    implementation(libs.recyclerview)

    implementation(project(":lib:core"))
    implementation(project(":lib:core-ui"))
    implementation(project(":lib:lists"))
    implementation(project(":lib:log"))
    implementation(project(":lib:rlottie"))

    implementation(project(":strings"))
    implementation(project(":uikit"))
}