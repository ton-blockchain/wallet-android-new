plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "${rootDir}/gradle/common.gradle")

android {
    namespace = "org.ton.wallet.data.tonclient.impl"
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.serializationJson)
    implementation(platform(libs.okHttpBom))
    implementation(libs.okHttpCore)
    implementation(libs.tonKotlin)

    implementation(project(":data:core"))
    implementation(project(":data:tonapi"))
    implementation(project(":data:tonclient-api"))
    implementation(project(":lib:log"))
}