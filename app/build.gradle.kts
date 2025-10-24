plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.app.music"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.app.music"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("sign.p12")
            storePassword = "8075"
            keyAlias = "sign"
            keyPassword = "8075"
            storeType = "pkcs12"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

//    buildFeatures {
//        compose = true
//    }

//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.5"
//    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }

//    kotlinOptions {
//        jvmTarget = "24"
//    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.10.00"))
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.0-alpha09")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0-alpha05")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0-alpha05")
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-session:1.8.0")
    implementation("androidx.media3:media3-common:1.8.0")
    implementation("androidx.datastore:datastore-preferences:1.2.0-alpha02")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}