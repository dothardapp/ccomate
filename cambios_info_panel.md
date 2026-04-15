# Cambios: Rediseño del ChannelInfoPanel + Fix de reproducción al reanudar

**Fecha:** 15 de abril de 2026  
**Módulo afectado:** `app-tv` y `core`  
**Flavors afectados:** ExoPlayer y VLC

---

## 1. Contexto del problema original

### Bug: Video no se reproduce al volver del botón Power / Home

Al presionar el botón **Power** o **Home** en TVs y TV BOX, la app arrancaba correctamente al volver pero el video **no se reproducía** — la pantalla quedaba negra. El usuario debía cambiar de canal y volver al original para que el stream arrancara.

**Causa raíz (3 problemas combinados):**

1. **`stopPlayer()` destruía el pipeline de reproducción** sin actualizar `_playerState.isPlaying`:
   - ExoPlayer: `stop()` ponía el player en `STATE_IDLE` y descartaba el `MediaSource`
   - VLC: `stop()` destruía el pipeline de decodificación interno

2. **`_playerState.isPlaying` quedaba como fantasma en `true`:**
   - `pausePlayer()` cambiaba el estado del player nativo pero nunca actualizaba `_playerState`
   - El handler de `STATE_IDLE` en el listener de ExoPlayer solo cancelaba el timeout, sin setear `isPlaying = false`
   - En VLC, los eventos `Paused`/`Stopped` son asincrónicos y podían no llegar a tiempo

3. **`playUrl()` tenía un early-return que bloqueaba la re-preparación:**
   ```kotlin
   if (_playerState.value.currentUrl == videoUrl && _playerState.value.isPlaying) {
       return  // ⛔ Bloqueaba porque isPlaying nunca se puso en false
   }
   ```

**Secuencia del bug:**
```
ON_PAUSE → pausePlayer() → isPlaying queda TRUE ⚠️
ON_STOP  → stopPlayer()  → pipeline destruido, isPlaying sigue TRUE ⚠️
ON_START → resumePlayer() → detecta IDLE → llama playUrl(url)
         → playUrl() → guard: currentUrl == url && isPlaying == TRUE → RETURN ⛔
         → RESULTADO: nunca se re-prepara el stream → pantalla negra
```

---

## 2. Archivos modificados

### 2.1 Fix de reproducción al reanudar (core)

#### `core/src/exoplayer/java/com/iptv/ccomate/viewmodel/VideoPlayerViewModel.kt`

**Cambio 1 — Handler de `STATE_IDLE` (línea ~115):**
```kotlin
// ANTES:
Player.STATE_IDLE -> {
    cancelBufferingTimeout()
}

// DESPUÉS:
Player.STATE_IDLE -> {
    cancelBufferingTimeout()
    _playerState.value = _playerState.value.copy(
        isBuffering = false,
        isPlaying = false
    )
}
```

**Cambio 2 — `pausePlayer()` (línea ~436):**
```kotlin
// ANTES:
fun pausePlayer() {
    exoPlayer?.let {
        it.playWhenReady = false
        it.pause()
    }
}

// DESPUÉS:
fun pausePlayer() {
    exoPlayer?.let {
        it.playWhenReady = false
        it.pause()
    }
    _playerState.value = _playerState.value.copy(isPlaying = false)
}
```

**Cambio 3 — `resumePlayer()` (línea ~442):**
```kotlin
// ANTES:
fun resumePlayer() {
    exoPlayer?.let {
        it.playWhenReady = true
    }
}

// DESPUÉS:
fun resumePlayer() {
    val player = exoPlayer ?: return
    val url = _playerState.value.currentUrl

    // Después de stopPlayer() (ON_STOP al apagar TV), ExoPlayer queda en STATE_IDLE
    // sin MediaSource. playWhenReady=true no tiene efecto en ese estado.
    if (player.playbackState == Player.STATE_IDLE && !url.isNullOrEmpty()) {
        Log.d("VideoPlayerVM", "Player in IDLE state after stop, re-preparing: $url")
        playUrl(url)
        return
    }

    player.playWhenReady = true
}
```

**Cambio 4 — `stopPlayer()` (línea ~458):**
```kotlin
// ANTES:
fun stopPlayer() {
    exoPlayer?.let {
        it.playWhenReady = false
        it.stop()
    }
}

// DESPUÉS:
fun stopPlayer() {
    exoPlayer?.let {
        it.playWhenReady = false
        it.stop()
    }
    _playerState.value = _playerState.value.copy(
        isPlaying = false,
        isBuffering = false
    )
}
```

---

#### `core/src/vlc/java/com/iptv/ccomate/viewmodel/VideoPlayerViewModel.kt`

**Cambio 1 — `pausePlayer()` (línea ~388):**
```kotlin
// ANTES:
fun pausePlayer() {
    mediaPlayer?.pause()
}

// DESPUÉS:
fun pausePlayer() {
    mediaPlayer?.pause()
    _playerState.value = _playerState.value.copy(isPlaying = false)
}
```

**Cambio 2 — `resumePlayer()` (línea ~392):**
```kotlin
// ANTES:
fun resumePlayer() {
    val player = mediaPlayer ?: return
    if (currentMedia != null && !player.isPlaying) {
        val url = _playerState.value.currentUrl
        if (url != null && player.media == null) {
            player.media = currentMedia
            player.play()
            return
        }
    }
    player.play()
}

// DESPUÉS:
fun resumePlayer() {
    val player = mediaPlayer ?: return
    val url = _playerState.value.currentUrl

    // Después de stopPlayer(), el pipeline de decodificación de VLC se destruye.
    // Un simple play() no funciona en estado Stopped.
    if (!player.isPlaying && !url.isNullOrEmpty()) {
        if (currentMedia != null) {
            // Re-asignar media forzosamente (incluso si player.media no es null,
            // el pipeline interno está destruido y necesita re-inicializarse).
            player.media = currentMedia
            player.play()
            Log.d(TAG, "Resumed from stopped state (re-assigned media): $url")
            return
        }
        // currentMedia fue liberado — re-crear todo desde cero
        Log.d(TAG, "Resumed from stopped state (full re-play): $url")
        playUrl(url)
        return
    }

    player.play()
}
```

**Cambio 3 — `stopPlayer()` (línea ~418):**
```kotlin
// ANTES:
fun stopPlayer() {
    mediaPlayer?.stop()
}

// DESPUÉS:
fun stopPlayer() {
    mediaPlayer?.stop()
    _playerState.value = _playerState.value.copy(
        isPlaying = false,
        isBuffering = false
    )
}
```

---

### 2.2 Rediseño del ChannelInfoPanel (app-tv)

#### `app-tv/src/main/java/com/iptv/ccomate/ui/screens/pluto/PlutoColors.kt`

Se agregaron 4 nuevos tokens de color al objeto `PlutoColors`:

```kotlin
// ── Estado de reproducción ──
val StatusLive = Color(0xFF4CAF50)       // Verde — reproduciendo en vivo
val StatusBuffering = Color(0xFFFFC107)  // Ámbar — cargando/buffering

// ── Barra de progreso EPG ──
val ProgressTrack = Color(0xFF3A3A3A)    // Track oscuro de la barra

// ── Divisor de secciones del panel ──
val DividerPanel = Color(0x66B0B0B0)     // Gris sutil con transparencia
```

---

#### `app-tv/src/main/java/com/iptv/ccomate/ui/screens/pluto/PlutoComponents.kt`

**Reescritura completa.** Se mantuvieron `TimeWarningBanner` y `StyledPanelBox` sin cambios. Se rediseñó `ChannelInfoPanel` y se agregaron 5 nuevos composables privados.

##### Nuevos composables:

| Composable | Función |
|------------|---------|
| `LiveClock` | Reloj digital (HH:mm, 36sp) con fecha en español. Se actualiza cada minuto, sincronizado al cambio exacto de minuto para evitar drift. |
| `PlaybackStatusRow` | Fila con dot de color + texto de estado ("En vivo", "Cargando...", "Error"). |
| `StatusDot` | Círculo de 10dp que pulsa (alpha oscilante 1→0.3) cuando el canal está en vivo. Usa `rememberInfiniteTransition`. |
| `EpgProgressBar` | `LinearProgressIndicator` que muestra el avance del programa actual. Calcula progreso con `Duration.between()`. Se actualiza cada 30 segundos. |
| `PanelSectionDivider` | `HorizontalDivider` sutil (0.5dp, color con transparencia) con spacing de 8dp arriba y abajo. |

##### Cambios en `ChannelInfoPanel`:

**Nuevos parámetros:**
```kotlin
// ANTES:
fun ChannelInfoPanel(
    channelLogo: String?,
    statusMessage: String,
    playbackError: Throwable?,
    currentProgram: EPGProgram?,
    showEpg: Boolean,
    modifier: Modifier = Modifier
)

// DESPUÉS:
fun ChannelInfoPanel(
    channelLogo: String?,
    channelName: String?,          // NUEVO — nombre del canal como texto
    statusMessage: String,
    playbackError: Throwable?,
    currentProgram: EPGProgram?,
    showEpg: Boolean,
    isPlaying: Boolean = false,    // NUEVO — estado de reproducción para el dot
    modifier: Modifier = Modifier
)
```

**Cambios de layout:**

- **Antes:** Todo el panel estaba envuelto en un `AnimatedVisibility` basado en `channelLogo`. Si no había logo, el panel completo desaparecía.
- **Ahora:** El panel (fondo + borde) **siempre es visible**. El reloj siempre se muestra. La info del canal y el EPG aparecen con `fadeIn` independiente.

**Estructura visual del panel rediseñado:**
```
┌─────────────────────────────┐
│         🕐 01:02            │  ← LiveClock (siempre visible)
│    Martes 15 de abril       │
│  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─    │  ← PanelSectionDivider
│      [LOGO CANAL]           │  ← AsyncImage (si hay logo)
│        ESPN HD              │  ← Nombre del canal (nuevo)
│      🟢 En vivo             │  ← PlaybackStatusRow (nuevo)
│  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─    │  ← PanelSectionDivider
│  🟢 AHORA                  │  ← Etiqueta con dot verde
│  Fútbol Liga Profesional    │  ← Título programa
│  21:00 — 23:00              │  ← Horario
│  ████████████░░░░░░░ 60%    │  ← EpgProgressBar (nuevo)
└─────────────────────────────┘
```

---

#### `app-tv/src/main/java/com/iptv/ccomate/ui/screens/pluto/PlutoTvScreen.kt`

Se actualizó la llamada a `ChannelInfoPanel` para pasar los nuevos parámetros:

```kotlin
infoPanelContent = { selectedChannel ->
    ChannelInfoPanel(
        channelLogo = selectedChannel?.logo,
        channelName = selectedChannel?.name,       // NUEVO
        statusMessage = uiState.statusMessage,
        playbackError = uiState.playbackError,
        currentProgram = uiState.currentProgram,
        showEpg = ENABLE_EPG_SIDE_PANEL,
        isPlaying = uiState.isPlaying              // NUEVO
    )
}
```

---

#### `app-tv/src/main/java/com/iptv/ccomate/ui/screens/tda/TDAScreen.kt`

Misma actualización que PlutoTvScreen:

```kotlin
infoPanelContent = { selectedChannel ->
    ChannelInfoPanel(
        channelLogo = selectedChannel?.logo,
        channelName = selectedChannel?.name,       // NUEVO
        statusMessage = uiState.statusMessage,
        playbackError = uiState.playbackError,
        currentProgram = null,
        showEpg = false,
        isPlaying = uiState.isPlaying              // NUEVO
    )
}
```

---

## 3. Dependencias y APIs utilizadas

| API / Clase | Paquete | Uso |
|-------------|---------|-----|
| `LinearProgressIndicator` | `androidx.compose.material3` | Barra de progreso EPG |
| `HorizontalDivider` | `androidx.compose.material3` | Divisor entre secciones |
| `rememberInfiniteTransition` | `androidx.compose.animation.core` | Pulsación del StatusDot |
| `LocalTime`, `LocalDate` | `java.time` (desugaring) | Reloj en tiempo real |
| `Duration.between()` | `java.time` (desugaring) | Cálculo de progreso EPG |
| `DateTimeFormatter` | `java.time.format` | Formato de hora y fecha |
| `Locale("es", "AR")` | `java.util` | Fecha en español argentino |

---

## 4. Notas para futuros cambios

### Si se quiere agregar "A continuación" (próximo programa EPG):
- Se necesita exponer el siguiente programa desde `PlutoTvViewModel`
- Los datos EPG ya están en `epgData` pero solo se busca el programa actual
- Agregar un campo `nextProgram: EPGProgram?` al `ChannelUiState`
- Agregar una sección adicional en `EpgInfoBlock` con el próximo programa

### Si se quiere cambiar la paleta de colores del panel:
- Todos los colores están centralizados en `PlutoColors.kt`
- Los nuevos tokens son: `StatusLive`, `StatusBuffering`, `ProgressTrack`, `DividerPanel`
- Los existentes que se usan: `TextPrimary`, `TextSecondary`, `TextSubtle`, `TextError`, `InfoPanelBackground`, `PanelBorder`

### Si se quiere modificar el formato del reloj:
- El composable `LiveClock` es privado en `PlutoComponents.kt`
- Formato de hora: `DateTimeFormatter.ofPattern("HH:mm")`
- Formato de fecha: `DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "AR"))`
- Intervalo de actualización: cada 60 segundos (sincronizado al cambio de minuto)

### Si se quiere modificar la barra de progreso EPG:
- `EpgProgressBar` recibe `startTime` y `endTime` del `EPGProgram`
- Se actualiza cada 30 segundos
- Usa `LinearProgressIndicator` de Material3 con forma lambda `progress = { value }`

### Sobre el ciclo de vida del player (funciones clave):
- `pausePlayer()` → Llamado en `ON_PAUSE`. Debe setear `_playerState.isPlaying = false`.
- `stopPlayer()` → Llamado en `ON_STOP`. Debe setear `_playerState.isPlaying = false` y `isBuffering = false`.
- `resumePlayer()` → Llamado en `ON_START`. Debe detectar si el player fue detenido con `stop()` y re-preparar el stream llamando `playUrl(url)`.
- `playUrl()` → Tiene un guard `currentUrl == url && isPlaying` que bloquea re-preparación si `isPlaying` quedó en `true`. Por eso es **crítico** que `stopPlayer()` y `pausePlayer()` actualicen `_playerState`.

### Archivos del lifecycle observer (donde se llama pause/stop/resume):
- ExoPlayer: `app-tv/src/exoplayer/java/com/iptv/ccomate/ui/video/VideoPlayer.kt` — `DisposableEffect(lifecycleOwner)`
- VLC: `app-tv/src/vlc/java/com/iptv/ccomate/ui/video/VideoPlayer.kt` — `DisposableEffect(lifecycleOwner)`
