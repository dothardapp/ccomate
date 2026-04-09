package com.iptv.ccomate.viewmodel

import com.iptv.ccomate.data.ChannelRepository
import com.iptv.ccomate.util.UrlPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TdaViewModel @Inject constructor(
    channelRepository: ChannelRepository,
    private val urlPreferences: UrlPreferences
) : ChannelListViewModel(channelRepository) {

    override val sourceName = "TDA"
    override val playlistUrl: String
        get() = urlPreferences.tdaPlaylistUrl

    init {
        initialize()
    }
}
