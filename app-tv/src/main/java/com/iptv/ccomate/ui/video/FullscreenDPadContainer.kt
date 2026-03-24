package com.iptv.ccomate.ui.video

import android.app.Activity
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.iptv.ccomate.model.Channel

/**
 * Contenedor fullscreen unificado con navegacion D-Pad para cambio de canal.
 *
 * Separa el foco en dos capas mutuamente excluyentes:
 * - Controlador D-Pad invisible (cuando NO hay error): captura UP/DOWN para cambiar canal.
 * - Boton Reintentar dentro de VideoPlayer (cuando HAY error): toma foco exclusivo.
 *
 * @param hasPlayerError indica si el VideoPlayer tiene un error activo.
 *        Cuando es true, el controlador D-Pad se elimina de composicion
 *        para que el boton Reintentar pueda recibir foco.
 */
@Composable
fun FullscreenDPadContainer(
    channels: List<Channel>,
    selectedChannelUrl: String?,
    hasPlayerError: Boolean = false,
    backgroundColor: Color = Color(0xFF121212),
    onChannelChanged: (Channel) -> Unit,
    onExitFullscreen: () -> Unit,
    content: @Composable () -> Unit
) {
    // Modo inmersivo: ocultar system bars al entrar en fullscreen
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        val controller = window?.let {
            WindowCompat.getInsetsController(it, it.decorView)
        }

        controller?.let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            controller?.let {
                it.show(WindowInsetsCompat.Type.systemBars())
                it.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
    }

    val dpadFocusRequester = remember { FocusRequester() }

    // Solicitar foco al controlador D-Pad al entrar en fullscreen (solo si no hay error)
    LaunchedEffect(hasPlayerError) {
        if (!hasPlayerError) {
            dpadFocusRequester.requestFocus()
        }
    }

    // Pre-calcular el índice fuera del handler de teclado
    val currentIndex by remember(channels, selectedChannelUrl) {
        derivedStateOf { channels.indexOfFirst { it.url == selectedChannelUrl } }
    }

    BackHandler { onExitFullscreen() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Capa 1: Contenido de video (sin foco propio)
        content()

        // Capa 2: Controlador D-Pad invisible (solo presente cuando NO hay error)
        // Cuando hay error, este nodo se quita de composicion y el boton Reintentar
        // dentro de VideoPlayer es el unico nodo focusable del arbol.
        if (!hasPlayerError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(dpadFocusRequester)
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && currentIndex != -1) {
                            when (event.nativeKeyEvent.keyCode) {
                                KeyEvent.KEYCODE_DPAD_UP -> {
                                    val prevIndex = if (currentIndex <= 0)
                                        channels.size - 1
                                    else
                                        currentIndex - 1
                                    onChannelChanged(channels[prevIndex])
                                    true
                                }
                                KeyEvent.KEYCODE_DPAD_DOWN -> {
                                    val nextIndex =
                                        (currentIndex + 1) % channels.size
                                    onChannelChanged(channels[nextIndex])
                                    true
                                }
                                KeyEvent.KEYCODE_DPAD_LEFT,
                                KeyEvent.KEYCODE_DPAD_RIGHT,
                                KeyEvent.KEYCODE_DPAD_CENTER,
                                KeyEvent.KEYCODE_ENTER -> true
                                else -> false
                            }
                        } else false
                    }
                    .focusable()
            )
        }
    }
}
