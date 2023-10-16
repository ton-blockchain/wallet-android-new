plugins {
    id("com.android.application") version "8.2.0-beta01" apply false
    id("com.android.library") version "8.2.0-beta01" apply false
    kotlin("android") version Config.Version.kotlin apply false
    kotlin("plugin.serialization") version Config.Version.kotlin apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}