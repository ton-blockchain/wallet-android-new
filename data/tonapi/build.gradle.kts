plugins {
    id("com.android.library")
}

android {
    namespace = "org.ton.wallet.data.tonapi"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    compileOptions {
        sourceCompatibility = Config.Version.javaSource
        targetCompatibility = Config.Version.javaTarget
    }
    sourceSets.getByName("main"){
        jniLibs.srcDir("./libs")
    }
}