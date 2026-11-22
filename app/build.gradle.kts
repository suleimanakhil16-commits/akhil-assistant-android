plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.akhil.assistant"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.akhil.assistant.test29"
        minSdk = 26
        targetSdk = 35
        versionCode = 29
        versionName = "1.0-test29"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
