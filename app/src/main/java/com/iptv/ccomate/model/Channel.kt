package com.iptv.ccomate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Channel(
    val name: String,
    val url: String,
    val logo: String?,
    val group: String?
) : Parcelable