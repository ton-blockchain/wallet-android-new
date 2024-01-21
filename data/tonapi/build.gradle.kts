plugins {
    alias(libs.plugins.androidLibrary)
}

apply(from = "${rootDir}/gradle/common-java.gradle")

android {
    namespace = "org.ton.wallet.data.tonapi"
    sourceSets.getByName("main"){
        jniLibs.srcDir("./libs")
    }
}