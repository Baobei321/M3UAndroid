package com.m3u.data.service

import android.graphics.Rect
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import kotlinx.coroutines.flow.MutableStateFlow

abstract class PlayerManager {
    abstract val player: Player
    val videoSize: MutableStateFlow<Rect> = MutableStateFlow(Rect())
    val playbackState: MutableStateFlow<@Player.State Int> = MutableStateFlow(Player.STATE_IDLE)
    val playerError: MutableStateFlow<PlaybackException?> = MutableStateFlow(null)

    abstract fun installMedia(url: String)
    abstract fun uninstallMedia()
}