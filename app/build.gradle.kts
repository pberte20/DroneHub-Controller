plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.aau.herd.controller"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aau.herd.controller"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false // Ensure you are using the new packaging options
            excludes += setOf(
                "META-INF/rxjava.properties"
            )
            keepDebugSymbols += setOf(
                "**/libdjivideo.so",
                "**/libSDKRelativeJNI.so",
                "**/libFlyForbid.so",
                "**/libduml_vision_bokeh.so",
                "**/libyuv2.so",
                "**/libGroudStation.so",
                "**/libFRCorkscrew.so",
                "**/libUpgradeVerify.so",
                "**/libFR.so",
                "**/libDJIFlySafeCore.so",
                "**/libdjifs_jni.so",
                "**/libsfjni.so"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.dji.sdk)
    compileOnly(libs.dji.sdk.provided)

    implementation(libs.socket.io.client) {
        exclude(group = "org.json", module = "json")
    }

    implementation(libs.stream.webrtc)
}