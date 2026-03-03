package com.iptv.ccomate.util

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf

val LocalFullscreenState =
        compositionLocalOf<MutableState<Boolean>> { error("No FullscreenState provided") }
