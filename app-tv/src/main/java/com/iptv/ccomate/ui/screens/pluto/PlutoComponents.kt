package com.iptv.ccomate.ui.screens.pluto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.iptv.ccomate.model.EPGProgram
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PanelCornerShape = RoundedCornerShape(16.dp)
private val LogoCornerShape = RoundedCornerShape(8.dp)

// ─────────────────────────────────────────────────────────────────────────────
// TimeWarningBanner
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Banner de advertencia que aparece cuando el reloj del dispositivo está mal configurado.
 */
@Composable
fun TimeWarningBanner(
    isVisible: Boolean,
    timeMessage: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(animationSpec = tween(300)),
        exit = fadeOut() + scaleOut(animationSpec = tween(300)),
        modifier = modifier
            .fillMaxWidth()
            .background(PlutoColors.WarningBannerBackground, PanelCornerShape)
            .border(1.dp, PlutoColors.WarningBannerBorder, PanelCornerShape)
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "⚠️ El reloj del dispositivo está mal configurado.",
                color = PlutoColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = timeMessage,
                color = PlutoColors.TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ChannelInfoPanel (layout horizontal optimizado para TV)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Panel lateral con layout optimizado para TV:
 *
 * ┌──────────────────────────────────────┐
 * │ [LOGO]          04:35               │  ← Logo izq + reloj der
 * │              Mié 15 de abril        │
 * │                                      │
 * │           🟢 En vivo                │  ← Estado
 * │                                      │
 * │ 🟢 Película de Acción              │  ← Programa actual
 * │    21:00 — 23:00                    │
 * │ ████████████░░░░░░                  │  ← Progreso
 * │                                      │
 * │ ▷ SportsCenter Nocturno            │  ← Siguiente programa
 * │    23:00 — 00:00                    │
 * └──────────────────────────────────────┘
 */
@Composable
fun ChannelInfoPanel(
    channelLogo: String?,
    channelName: String?,
    statusMessage: String,
    playbackError: Throwable?,
    currentProgram: EPGProgram?,
    showEpg: Boolean,
    isPlaying: Boolean = false,
    nextProgram: EPGProgram? = null,
    modifier: Modifier = Modifier
) {
    val hasChannel = !channelName.isNullOrBlank() || !channelLogo.isNullOrBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .shadow(2.dp, PanelCornerShape)
            .clip(PanelCornerShape)
            .background(PlutoColors.InfoPanelBackground)
            .border(0.5.dp, PlutoColors.PanelBorder, PanelCornerShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Fila 1: Logo (izq) + Reloj/Fecha (der) ──
        HeaderRow(channelLogo = channelLogo)

        Spacer(modifier = Modifier.height(10.dp))

        // ── Estado de reproducción (centrado) ──
        AnimatedVisibility(
            visible = hasChannel,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(300))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                PlaybackStatusRow(
                    isPlaying = isPlaying,
                    playbackError = playbackError
                )

                // Detalle del error
                if (playbackError != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = playbackError.localizedMessage ?: "Error desconocido",
                        color = PlutoColors.TextError,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // ── EPG: programa actual + barra + siguiente ──
        AnimatedVisibility(
            visible = showEpg && currentProgram != null,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(300))
        ) {
            currentProgram?.let { program ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // ── Programa actual ──
                    CurrentProgramBlock(program)

                    // ── Siguiente programa ──
                    if (nextProgram != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        NextProgramBlock(nextProgram)
                    }
                }
            }
        }

        // ── Mensaje de carga (solo cuando no hay canal) ──
        if (!hasChannel && statusMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage,
                fontSize = 14.sp,
                color = PlutoColors.TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HeaderRow — Logo (izquierda) + Reloj/Fecha (derecha)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeaderRow(channelLogo: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo del canal (izquierda)
        val context = LocalContext.current
        if (!channelLogo.isNullOrBlank()) {
            AsyncImage(
                model = remember(channelLogo) {
                    ImageRequest.Builder(context)
                        .data(channelLogo)
                        .size(160, 96)
                        .crossfade(true)
                        .build()
                },
                contentDescription = "Logo del canal",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(width = 80.dp, height = 44.dp)
                    .clip(LogoCornerShape)
                    .background(Color.DarkGray)
            )
        } else {
            // Placeholder para mantener el layout
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 44.dp)
                    .clip(LogoCornerShape)
                    .background(Color.DarkGray.copy(alpha = 0.3f))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Reloj + Fecha (derecha)
        LiveClock(modifier = Modifier.weight(1f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LiveClock — reloj digital con fecha alineado a la derecha
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LiveClock(modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            currentDate = LocalDate.now()
            val secondsUntilNextMinute = 60 - LocalTime.now().second
            delay(secondsUntilNextMinute * 1000L)
        }
    }

    val timeText = remember(currentTime.hour, currentTime.minute) {
        currentTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
    val dateText = remember(currentDate) {
        currentDate.format(
            DateTimeFormatter.ofPattern("EEE d 'de' MMMM", Locale("es", "AR"))
        ).replace(".", "").replaceFirstChar { it.uppercase() }
    }

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        Text(
            text = timeText,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PlutoColors.TextPrimary
        )
        Text(
            text = dateText,
            fontSize = 14.sp,
            color = PlutoColors.TextSubtle
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PlaybackStatusRow
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlaybackStatusRow(
    isPlaying: Boolean,
    playbackError: Throwable?
) {
    val dotColor = when {
        playbackError != null -> PlutoColors.TextError
        isPlaying -> PlutoColors.StatusLive
        else -> PlutoColors.StatusBuffering
    }
    val statusLabel = when {
        playbackError != null -> "Error"
        isPlaying -> "En vivo"
        else -> "Cargando..."
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        StatusDot(color = dotColor, pulsate = isPlaying && playbackError == null)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = statusLabel,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (playbackError != null) PlutoColors.TextError else PlutoColors.TextSecondary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// StatusDot
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatusDot(
    color: Color,
    pulsate: Boolean,
    modifier: Modifier = Modifier
) {
    val dotAlpha = if (pulsate) {
        val transition = rememberInfiniteTransition(label = "statusPulse")
        val alpha by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
        alpha
    } else {
        1f
    }

    Box(
        modifier = modifier
            .size(8.dp)
            .background(color.copy(alpha = dotAlpha), CircleShape)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// CurrentProgramBlock — programa actual con barra de progreso
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CurrentProgramBlock(program: EPGProgram) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val start = program.startTime.format(timeFormatter)
    val end = program.endTime.format(timeFormatter)

    // Título con dot
    Row(verticalAlignment = Alignment.CenterVertically) {
        StatusDot(color = PlutoColors.StatusLive, pulsate = true)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = program.title,
            color = PlutoColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }

    // Horario
    Text(
        text = "$start — $end",
        color = PlutoColors.TextSubtle,
        fontSize = 14.sp,
        modifier = Modifier.padding(start = 14.dp, top = 1.dp)
    )

    // Barra de progreso
    Spacer(modifier = Modifier.height(4.dp))
    EpgProgressBar(startTime = program.startTime, endTime = program.endTime)
}

// ─────────────────────────────────────────────────────────────────────────────
// NextProgramBlock — siguiente programa
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NextProgramBlock(program: EPGProgram) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val start = program.startTime.format(timeFormatter)
    val end = program.endTime.format(timeFormatter)

    // Título con icono "siguiente"
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "▷",
            fontSize = 14.sp,
            color = PlutoColors.TextSubtle
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = program.title,
            color = PlutoColors.TextSecondary,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }

    // Horario
    Text(
        text = "$start — $end",
        color = PlutoColors.TextSubtle,
        fontSize = 14.sp,
        modifier = Modifier.padding(start = 20.dp, top = 1.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// EpgProgressBar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EpgProgressBar(
    startTime: java.time.ZonedDateTime,
    endTime: java.time.ZonedDateTime
) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(startTime, endTime) {
        while (true) {
            val now = java.time.ZonedDateTime.now()
            val totalSeconds = Duration.between(startTime, endTime).seconds.toFloat()
            val elapsedSeconds = Duration.between(startTime, now).seconds.toFloat()
            progress = if (totalSeconds > 0f) {
                (elapsedSeconds / totalSeconds).coerceIn(0f, 1f)
            } else {
                0f
            }
            delay(30_000L)
        }
    }

    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
        color = PlutoColors.StatusLive,
        trackColor = PlutoColors.ProgressTrack
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// StyledPanelBox
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StyledPanelBox(
    modifier: Modifier = Modifier,
    gradient: androidx.compose.ui.graphics.Brush = PlutoColors.PanelGradient,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(2.dp, PanelCornerShape)
            .clip(PanelCornerShape)
            .background(gradient)
            .border(0.5.dp, PlutoColors.PanelBorder, PanelCornerShape),
        content = content
    )
}
