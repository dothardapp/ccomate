# ════════════════════════════════════════════════════════════════════════════
# CCOMate IPTV — ProGuard / R8 Rules
# ════════════════════════════════════════════════════════════════════════════

# Mantener información de línea para stack traces legibles en producción
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ─── Modelos de dominio ──────────────────────────────────────────────────────
# Channel, EPGProgram, DrawerItem y demás modelos del paquete model
# R8 los ofuscaría y rompería la deserialización de Room / Ktor
-keep class com.iptv.ccomate.model.** { *; }
-keepclassmembers class com.iptv.ccomate.model.** { *; }

# ─── Room Database ───────────────────────────────────────────────────────────
# Entities: R8 no debe renombrar campos usados como columnas de BD
-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }

# DAOs: generados por KSP, deben mantenerse completos
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Entidades del core (package específico)
-keep class com.iptv.ccomate.data.local.** { *; }
-keepclassmembers class com.iptv.ccomate.data.local.** { *; }

# ─── Hilt / Dagger ───────────────────────────────────────────────────────────
# Hilt genera clases en tiempo de compilación; no ofuscar sus nombres
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ViewModels anotados con @HiltViewModel
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel { *; }

# Módulos y componentes Hilt generados
-keep class **_HiltModules* { *; }
-keep class **_GeneratedInjector { *; }
-keep class *_MembersInjector { *; }
-keep class *_Factory { *; }

# ─── Kotlin ──────────────────────────────────────────────────────────────────
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { *; }

# Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ─── Coil ────────────────────────────────────────────────────────────────────
-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**

# ─── Ktor (HTTP client) ──────────────────────────────────────────────────────
-keep class io.ktor.** { *; }
-keep interface io.ktor.** { *; }
-dontwarn io.ktor.**

# Ktor usa reflexión para serializers
-keepclassmembers class io.ktor.** {
    volatile <fields>;
    public <methods>;
}

# ─── OkHttp ──────────────────────────────────────────────────────────────────
# OkHttp incluye sus propias reglas en el AAR, pero por si acaso:
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ─── Media3 / ExoPlayer ──────────────────────────────────────────────────────
# ExoPlayer incluye reglas en su AAR; estas cubren el código @UnstableApi
# y los extractors/renderers que se instancian por reflexión
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }
-dontwarn androidx.media3.**

# TS Extractor flags (usados explícitamente en VideoPlayerViewModel)
-keep class androidx.media3.extractor.ts.** { *; }
-keep class androidx.media3.extractor.DefaultExtractorsFactory { *; }

# ─── VLC (flavor vlc) ────────────────────────────────────────────────────────
# LibVLC usa JNI extensamente
-keep class org.videolan.** { *; }
-keep interface org.videolan.** { *; }
-dontwarn org.videolan.**

# ─── AndroidX / Compose ──────────────────────────────────────────────────────
# Compose Runtime y Navigation usan reflexión para Composables y rutas
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Navigation Compose usa clase sellada para rutas
-keep class * extends androidx.navigation.NavGraph { *; }

# ─── Lifecycle / ViewModel ───────────────────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ─── Commons Net (NTP) ───────────────────────────────────────────────────────
-dontwarn org.apache.commons.net.**
-keep class org.apache.commons.net.ntp.** { *; }

# ─── SLF4J ───────────────────────────────────────────────────────────────────
-dontwarn org.slf4j.**

# ─── Advertencias generales a suprimir ───────────────────────────────────────
-dontwarn sun.misc.**
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
