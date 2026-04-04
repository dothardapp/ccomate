import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

// ── Carga de credenciales de firma ────────────────────────────────────────────
val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
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

    // ── Firma ─────────────────────────────────────────────────────────────────
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")

            // ── R8 / minificación ────────────────────────────────────────────
            isMinifyEnabled = true          // Activa R8: minifica + ofusca + optimiza
            isShrinkResources = true        // Elimina recursos no referenciados

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), // Reglas base de Android con optimizaciones
                "proguard-rules.pro"                                      // Reglas del proyecto
            )
        }
        debug {
            // Debug sin minificación para builds rápidos de desarrollo
            isMinifyEnabled = false
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
    flavorDimensions += "player"
    productFlavors {
        create("exoplayer") {
            dimension = "player"
        }
        create("vlc") {
            dimension = "player"
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // ── Core module (modelos, data, DI, viewmodels, util) ──
    implementation(project(":core"))

    // ── Hilt (KSP necesario en cada módulo app) ──
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // ── Compose UI ──
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.material3)

    // ── TV Material ──
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)

    // ── Activity + Lifecycle ──
    implementation(libs.androidx.activity.compose)

    // ── AndroidX core ──
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // ── Tests ──
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
