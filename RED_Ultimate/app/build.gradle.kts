plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.red.sovereign"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.red.sovereign"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0-RED"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {
    // RED Core Security
    implementation(project(":lib:libsignal-service"))
    implementation(libs.libsignal.android)
    implementation(libs.signal.android.database.sqlcipher)

    // System A: 1080p WebRTC
    implementation(libs.signal.ringrtc)
    implementation(libs.bundles.media3)

    // System B: GSM Gateway
    implementation("org.asteriskjava:asterisk-java:3.40.0")

    // Master DI (Hilt)
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")

    // UI & Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
