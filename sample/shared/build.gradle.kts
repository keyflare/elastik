plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.cocoapods)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "shared"
        }
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(libs.compose.android.ui)
                implementation(libs.compose.android.ui.tooling.preview)
            }
        }
         val commonMain by getting {
            dependencies {
                implementation(libs.coroutines)
                implementation(libs.kermit)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)

                implementation(project(":elastik:elastik-compose"))
            }
        }
    }
}

android {
    namespace = "com.keyflare.sample.shared"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependencies {
        debugImplementation(libs.compose.android.ui.tooling)
    }
}
