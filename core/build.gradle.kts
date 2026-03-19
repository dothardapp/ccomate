plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.iptv.ccomate.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // ── Hilt (api para que app-tv vea las anotaciones) ──
    api(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // ── Room ──
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ── Media3 ExoPlayer ──
    api(libs.androidx.media3.exoplayer)
    api(libs.androidx.media3.exoplayer.hls)
    api(libs.androidx.media3.ui)
    api(libs.androidx.media3.datasource.okhttp)

    // ── Lifecycle + Compose (ViewModels exponen StateFlow) ──
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.lifecycle.runtime.ktx)

    // ── Ktor HTTP client ──
    api(libs.ktor.client.core)
    api(libs.ktor.client.cio)

    // ── OkHttp ──
    api(libs.okhttp)

    // ── NTP (commons-net) ──
    api(libs.commons.net)

    // ── SLF4J ──
    implementation(libs.slf4j.android)

    // ── Coil (SvgUtils) ──
    api(libs.coil.compose)
    api(libs.coil.svg)

    // ── AndroidX core ──
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // ── Compose (necesario para CompositionLocal y utilidades de UI en core) ──
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)

    // ── Tests ──
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
