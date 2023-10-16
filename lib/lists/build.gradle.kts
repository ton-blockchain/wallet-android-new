plugins {
    id("com.android.library")
}

android {
    namespace = "org.ton.wallet.lib.lists"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
    compileOptions {
        sourceCompatibility = Config.Version.javaSource
        targetCompatibility = Config.Version.javaTarget
    }
}

dependencies {
    implementation(Config.Lib.recyclerView)
}