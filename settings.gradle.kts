pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TonWallet"

include(":app")
include(":strings")
include(":uicomponents")
include(":uikit")

// lib
include(":lib:core")
include(":lib:core-ui")
include(":lib:di")
include(":lib:lists")
include(":lib:log")
include(":lib:qr-builder")
include(":lib:rlottie")
include(":lib:screen")
include(":lib:security")
include(":lib:sqlite")
include(":lib:sqlite-lib")

// data
include(":data:auth-api")
include(":data:auth-impl")
include(":data:core")
include(":data:notifications-api")
include(":data:notifications-impl")
include(":data:prices-api")
include(":data:prices-impl")
include(":data:settings-api")
include(":data:settings-impl")
include(":data:tonapi")
include(":data:tonclient-api")
include(":data:tonclient-impl")
include(":data:tonconnect-api")
include(":data:tonconnect-impl")
include(":data:transactions-api")
include(":data:transactions-impl")
include(":data:wallet-api")
include(":data:wallet-impl")

// domain
include(":domain:blockchain-api")
include(":domain:blockchain-impl")
include(":domain:settings-api")
include(":domain:settings-impl")
include(":domain:tonconnect-api")
include(":domain:tonconnect-impl")
include(":domain:transactions-api")
include(":domain:transactions-impl")
include(":domain:wallet-api")
include(":domain:wallet-impl")

// feature
include(":feature:onboarding-api")
include(":feature:onboarding-impl")
include(":feature:passcode-api")
include(":feature:passcode-impl")
include(":feature:scanqr-api")
include(":feature:scanqr-impl")
include(":feature:send-api")
include(":feature:send-impl")
include(":feature:settings-api")
include(":feature:settings-impl")
include(":feature:tonconnect-api")
include(":feature:tonconnect-impl")
include(":feature:transactions-api")
include(":feature:transactions-impl")
include(":feature:wallet-api")
include(":feature:wallet-impl")
