package com.iptv.ccomate.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Size
import coil.decode.SvgDecoder


@Composable
fun rememberSvgPainter(resourceId: Int): Painter {
    val context = LocalContext.current

    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(resourceId)
            .size(Size.ORIGINAL)
            .build(),
        imageLoader = imageLoader
    )
}
