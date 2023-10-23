pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "elastik"

// Library
include(":elastik")
include(":elastik:elastik-core")
include(":elastik:elastik-compose")

// Samples
include(":sample:app-android")
include(":sample:shared")
