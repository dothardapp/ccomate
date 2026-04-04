package com.iptv.ccomate.mobile.ui.video

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptv.ccomate.viewmodel.VideoPlayerViewModel
import org.videolan.libvlc.util.VLCVideoLayout

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    channelName: String? = null,
    onPlaybackStarted: (() -> Unit)? = null,
    onPlaybackError: ((Throwable) -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context.findActivity()
        ?: throw IllegalStateException("VideoPlayer debe ser alojado en un ComponentActivity")
    val viewModel: VideoPlayerViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Trigger playUrl cuando cambia la URL
    LaunchedEffect(videoUrl) {
        Log.d("VideoPlayer", "URL changed: $videoUrl")
        viewModel.playUrl(videoUrl)
    }

    // Notificar al padre sobre playback
    LaunchedEffect(playerState.isPlaying) {
        if (playerState.isPlaying) onPlaybackStarted?.invoke()
    }

    // Notificar al padre sobre errores
    LaunchedEffect(playerState.hasError, playerState.errorMessage) {
        if (playerState.hasError && playerState.errorMessage.isNotEmpty()) {
            onPlaybackError?.invoke(Exception(playerState.errorMessage))
        }
    }

    // Lifecycle: pause/resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.pausePlayer()
                Lifecycle.Event.ON_START -> viewModel.resumePlayer()
                Lifecycle.Event.ON_STOP -> viewModel.stopPlayer()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Liberar player cuando este Composable sale de composicion,
    // pero SOLO si la URL actual del ViewModel coincide con la nuestra.
    DisposableEffect(Unit) {
        onDispose {
            if (viewModel.playerState.value.currentUrl == videoUrl) {
                viewModel.releasePlayer()
            }
        }
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // VLCVideoLayout — superficie unica reutilizada.
        // movableContentOf en MobileChannelScreen preserva esta vista durante rotacion.
        // El telon opaco de buffering oculta ghost frames al cambiar canal.
        AndroidView(
            factory = { ctx ->
                VLCVideoLayout(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    keepScreenOn = true
                    viewModel.attachViews(this)

                    // Cuando movableContentOf mueve esta vista a otro padre
                    // (portrait ↔ landscape), la superficie VLC se destruye.
                    // Re-vincular al detectar que la vista volvio al arbol de ventana.
                    addOnAttachStateChangeListener(object : android.view.View.OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: android.view.View) {
                            // Diferir al siguiente frame — la Surface del SurfaceView
                            // interno de VLC no esta lista hasta despues del layout pass.
                            v.post { viewModel.attachViews(v as VLCVideoLayout) }
                        }
                        override fun onViewDetachedFromWindow(v: android.view.View) {
                            // No desvincular aqui — la vista puede estar siendo movida,
                            // no destruida. La desvinculacion real ocurre en onRelease.
                        }
                    })
                }
            },
            onRelease = { layout ->
                viewModel.detachViews(layout)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Telon de Carga — fondo opaco que oculta el frame congelado de VLC
        if (playerState.isBuffering && !playerState.hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Telon de Error — fondo opaco que bloquea visualmente al VLC congelado
        if (playerState.hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${playerState.errorMessage}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
