# ════════════════════════════════════════════════════════════════════════════
# CCOMate :core — Consumer ProGuard Rules
# Estas reglas se aplican automáticamente en cualquier módulo que dependa de :core
# ════════════════════════════════════════════════════════════════════════════

# ─── Room Entities y DAOs (core expone estos via api()) ──────────────────────
-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class com.iptv.ccomate.data.local.** { *; }

# ─── Modelos de dominio ──────────────────────────────────────────────────────
-keep class com.iptv.ccomate.model.** { *; }
-keepclassmembers class com.iptv.ccomate.model.** { *; }

# ─── ViewModels (expuestos via api()) ────────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ─── Repositorios y utilidades del core ──────────────────────────────────────
-keep class com.iptv.ccomate.data.** { *; }
-keep class com.iptv.ccomate.util.** { *; }
-keep class com.iptv.ccomate.viewmodel.** { *; }
