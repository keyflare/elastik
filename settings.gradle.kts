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

// Samples
include(":sample:app-android")
include(":elastik:elastik-core")
include(":elastik:elastik-routing")
include(":elastik:elastik-render")
include(":sample:shared")
include(":elastik-compose")
