plugins {
    id("com.android.library")
}

android {
    namespace = "org.ton.wallet.lib.qr"
    compileSdk = Config.Build.compileSdk
    defaultConfig {
        minSdk = Config.Build.minSdk
    }
}