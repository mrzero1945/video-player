plugins {
    id("com.android.application")
}

android {
    namespace = "com.mrzero.videoplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mrzero.videoplayer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core:1.16.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.collection:collection:1.5.0")
    implementation("androidx.media:media:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
}
