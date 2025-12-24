import java.util.*

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
}

val localProperties = Properties()
val localPropertiesFile: File = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "ru.gdlbo.parcelradar.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.gdlbo.parcelradar.app"
        minSdk = 23
        targetSdk = 36
        versionCode = 11
        versionName = "2.1"

        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    signingConfigs {
        create("key") {
            storeFile = file(localProperties.getProperty("KEY_STORE_FILE", "key.jks"))
            storePassword = localProperties.getProperty("KEY_STORE_PASSWORD", "null")
            keyAlias = localProperties.getProperty("KEY_ALIAS", "alias")
            keyPassword = localProperties.getProperty("KEY_PASSWORD", "null")

            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("key")
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("key")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_23
        targetCompatibility = JavaVersion.VERSION_23
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "36.1.0"
    ndkVersion = "26.3.11579264"
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Material Icons
    implementation(libs.material.icons.core)
    implementation(libs.material.icons.extended)

    // Image Loading
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    // Networking
    implementation(libs.bundles.ktor)
    implementation(libs.kotlinx.serialization.json)

    // DI & Architecture
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.bundles.decompose)

    // Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Camera
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Background Work
    implementation(libs.androidx.work.runtime.ktx)

    // QR Code
    implementation(libs.zxing.core)
}