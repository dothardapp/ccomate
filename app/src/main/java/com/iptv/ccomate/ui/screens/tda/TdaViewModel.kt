package com.iptv.ccomate.ui.screens.tda

import com.iptv.ccomate.data.ChannelRepository
import com.iptv.ccomate.ui.screens.base.ChannelListViewModel
import com.iptv.ccomate.util.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TdaViewModel @Inject constructor(
    channelRepository: ChannelRepository
) : ChannelListViewModel(channelRepository) {

    override val sourceName = "TDA"
    override val playlistUrl = AppConfig.TDA_PLAYLIST_URL

    init {
        initialize()
    }
}
