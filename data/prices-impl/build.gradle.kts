plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.data.prices.impl"
}

dependencies {
    implementation(libs.coroutines)
    implementation(platform(libs.okHttpBom))
    implementation(libs.okHttpCore)

    implementation(project(":data:core"))
    implementation(project(":data:prices-api"))
    implementation(project(":lib:log"))
    implementation(project(":lib:sqlite"))
}