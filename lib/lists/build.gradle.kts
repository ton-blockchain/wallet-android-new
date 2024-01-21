plugins {
    alias(libs.plugins.androidLibrary)
}

apply(from = "${rootDir}/gradle/common-java.gradle")

android {
    namespace = "org.ton.wallet.lib.lists"
}

dependencies {
    implementation(libs.recyclerview)
}