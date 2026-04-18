package com.iptv.ccomate.core.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design tokens centralizados del sistema CCOMate.
 *
 * Ambas plataformas (TV y Mobile) consumen estos valores directamente
 * o a través de wrappers de compatibilidad (AppColors, MobileColors).
 *
 * Estructura:
 * - [Colors]     → Paleta "cinema dark" (charcoal + blue accent), TV-safe
 * - [Elevation]  → Niveles de sombra semánticos
 * - [Radius]     → Esquinas redondeadas consistentes
 * - [Motion]     → Duraciones y easings para animaciones
 *
 * @see <a href="https://github.com/dothardapp/ccomate/issues/88">Issue #88</a>
 */
object DesignTokens {

    // ═════════════════════════════════════════════════════════════════════════
    // COLORS — Paleta "cinema dark" con accent azul
    // ═════════════════════════════════════════════════════════════════════════
    object Colors {

        // ── Base dark ──
        /** Fondo general de la aplicación — casi negro, TV-safe */
        val bgBase       = Color(0xFF0E1116)
        /** Superficies elevadas: panels, cards */
        val bgElevated   = Color(0xFF161B22)
        /** Fondo de focus/hover e items destacados */
        val bgHighlight  = Color(0xFF21262D)
        /** Surface intermedio entre base y elevated */
        val bgSurface    = Color(0xFF1C2128)

        // ── Accent principal ──
        /** Color primario de marca — azul saturado */
        val accent       = Color(0xFF4F8EF7)
        /** Variante clara para focused/hover */
        val accentBright = Color(0xFF6BA3FF)
        /** Variante oscura para borders sutiles */
        val accentSoft   = Color(0xFF2D5FBA)
        /** Accent 40% alpha — para halos/shadows de glow */
        val accentGlow   = Color(0x664F8EF7)

        // ── Estados semánticos ──
        /** En vivo, OK */
        val success      = Color(0xFF3FB950)
        /** Buffering, advertencia */
        val warning      = Color(0xFFD29922)
        /** Error */
        val error        = Color(0xFFF85149)

        // ── Texto (TV-safe, sin blanco puro) ──
        val textPrimary   = Color(0xFFE6EDF3)
        val textSecondary = Color(0xFFB1BAC4)
        val textTertiary  = Color(0xFF8B949E)
        val textSubtle    = Color(0xFF6E7681)

        // ── Overlays ──
        /** Overlay oscuro traslúcido sobre video */
        val overlayDark   = Color(0xCC0E1116)
        /** Más denso — para pantallas de error */
        val overlayDarker = Color(0xE60E1116)
        /** Panel sobre video */
        val overlayPanel  = Color(0xAB0E1116)

        // ── Divisores ──
        val divider       = Color(0xFF30363D)
        val dividerSoft   = Color(0x4D30363D)
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ELEVATION — Niveles de sombra semánticos
    // ═════════════════════════════════════════════════════════════════════════
    object Elevation {
        val none:   Dp = 0.dp
        val low:    Dp = 2.dp
        val medium: Dp = 4.dp
        val high:   Dp = 8.dp
        val extra:  Dp = 16.dp
    }

    // ═════════════════════════════════════════════════════════════════════════
    // RADIUS — Esquinas redondeadas consistentes
    // ═════════════════════════════════════════════════════════════════════════
    object Radius {
        val sm: Dp =  8.dp
        val md: Dp = 12.dp
        val lg: Dp = 16.dp
        val xl: Dp = 24.dp
    }

    // ═════════════════════════════════════════════════════════════════════════
    // MOTION — Duraciones (ms) y easings para animaciones
    // ═════════════════════════════════════════════════════════════════════════
    object Motion {
        /** Transiciones rápidas: toggles, hover */
        const val fast:   Int = 150
        /** Transiciones estándar: expand, collapse */
        const val normal: Int = 250
        /** Transiciones enfáticas: entrada de pantalla */
        const val slow:   Int = 400

        /** Easing suave — idéntico a FastOutSlowInEasing */
        val emphasizedEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        /** Easing de desaceleración */
        val decelerateEasing: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    }
}
