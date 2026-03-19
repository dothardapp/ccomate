package com.iptv.ccomate.ui.screens.pluto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.iptv.ccomate.model.EPGProgram
import java.time.format.DateTimeFormatter

private val PanelCornerShape = RoundedCornerShape(12.dp)
private val LogoCornerShape = RoundedCornerShape(6.dp)

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

/**
 * Panel lateral con el logo del canal, estado de reproducción e información EPG.
 */
@Composable
fun ChannelInfoPanel(
    channelLogo: String?,
    statusMessage: String,
    playbackError: Throwable?,
    currentProgram: EPGProgram?,
    showEpg: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !channelLogo.isNullOrBlank(),
        enter = fadeIn() + scaleIn(animationSpec = tween(300)),
        exit = fadeOut() + scaleOut(animationSpec = tween(300)),
        modifier = modifier
            .fillMaxSize()
            .shadow(2.dp, PanelCornerShape)
            .clip(PanelCornerShape)
            .background(PlutoColors.InfoPanelBackground)
            .border(0.5.dp, PlutoColors.PanelBorder, PanelCornerShape)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo del canal
            AsyncImage(
                model = channelLogo,
                contentDescription = "Logo del canal",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(width = 130.dp, height = 72.dp)
                    .clip(LogoCornerShape)
                    .background(Color.DarkGray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Estado de reproducción
            Text(
                text = statusMessage,
                color = if (playbackError == null) PlutoColors.TextPrimary else PlutoColors.TextError,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Información EPG
            if (showEpg && currentProgram != null) {
                EpgInfoBlock(currentProgram)
            }

            // Detalle del error
            if (playbackError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Error: ${playbackError.localizedMessage ?: "desconocido"}",
                    color = PlutoColors.TextError,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Bloque con título y horario del programa EPG actual.
 */
@Composable
private fun EpgInfoBlock(program: EPGProgram) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val start = program.startTime.format(timeFormatter)
    val end = program.endTime.format(timeFormatter)

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = program.title,
        color = PlutoColors.TextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Text(
        text = "$start - $end",
        color = PlutoColors.TextSubtle,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp)
    )
}

/**
 * Contenedor estilizado reutilizable para paneles con gradiente y borde.
 */
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
