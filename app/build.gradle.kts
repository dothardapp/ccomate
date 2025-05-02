plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.iptv.ccomate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.iptv.ccomate"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation)

    //Libreria Material 3
    implementation(libs.androidx.material3)

    //Librerias para TV
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)

    //Libreria COIL para cargar imagenes desde internet
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)


    //Libreria media3 ExoPlayer
    implementation (libs.androidx.media3.exoplayer)
    implementation (libs.androidx.media3.ui)
    implementation(libs.androidx.media3.exoplayer.hls)

    //Esto permite hacer requests HTTP con Ktor, que es liviano y se lleva bien con Compose.
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    //Contro del ciclo de vida de las actividades
    implementation(libs.androidx.lifecycle.runtime.compose)

    //Para consultas a NTP
    implementation(libs.commons.net)

    //Librerias para el splash screen
    implementation (libs.androidx.animation)

    implementation(libs.slf4j.android)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}