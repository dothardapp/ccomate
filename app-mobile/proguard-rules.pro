# ════════════════════════════════════════════════════════════════════════════
# CCOMate IPTV Mobile — ProGuard / R8 Rules
# ════════════════════════════════════════════════════════════════════════════

# Mantener información de línea para stack traces legibles en producción
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ─── Modelos de dominio ──────────────────────────────────────────────────────
-keep class com.iptv.ccomate.model.** { *; }
-keepclassmembers class com.iptv.ccomate.model.** { *; }

# ─── Room Database ───────────────────────────────────────────────────────────
-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class com.iptv.ccomate.data.local.** { *; }
-keepclassmembers class com.iptv.ccomate.data.local.** { *; }

# ─── Hilt / Dagger ───────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel { *; }
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

# ─── Ktor (HTTP client, usado en :core) ─────────────────────────────────────
-keep class io.ktor.** { *; }
-keep interface io.ktor.** { *; }
-dontwarn io.ktor.**
-keepclassmembers class io.ktor.** {
    volatile <fields>;
    public <methods>;
}

# ─── OkHttp ──────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ─── Media3 / ExoPlayer (flavor exoplayer) ───────────────────────────────────
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keep class androidx.media3.extractor.ts.** { *; }
-keep class androidx.media3.extractor.DefaultExtractorsFactory { *; }

# ─── VLC (flavor vlc) ────────────────────────────────────────────────────────
-keep class org.videolan.** { *; }
-keep interface org.videolan.** { *; }
-dontwarn org.videolan.**

# ─── AndroidX / Compose ──────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class * extends androidx.navigation.NavGraph { *; }

# ─── Lifecycle / ViewModel ───────────────────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ─── Commons Net (NTP, usado en :core) ───────────────────────────────────────
-dontwarn org.apache.commons.net.**
-keep class org.apache.commons.net.ntp.** { *; }

# ─── SLF4J ───────────────────────────────────────────────────────────────────
-dontwarn org.slf4j.**

# ─── Advertencias generales a suprimir ───────────────────────────────────────
-dontwarn sun.misc.**
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
