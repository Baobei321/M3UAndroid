package com.m3u.smartphone.ui.business.playlist.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.m3u.core.architecture.preferences.hiltPreferences
import com.m3u.data.database.model.Channel
import com.m3u.data.database.model.Programme
import com.m3u.business.playlist.PlaylistViewModel
import com.m3u.core.foundation.components.CircularProgressIndicator
import com.m3u.smartphone.ui.material.components.VerticalDraggableScrollbar
import com.m3u.smartphone.ui.material.ktx.plus
import com.m3u.smartphone.ui.material.model.LocalSpacing
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun ChannelGallery(
    state: LazyStaggeredGridState,
    rowCount: Int,
    categoryWithChannels: PlaylistViewModel.CategoryWithChannels?,
    zapping: Channel?,
    recently: Boolean,
    isVodOrSeriesPlaylist: Boolean,
    onClick: (Channel) -> Unit,
    onLongClick: (Channel) -> Unit,
    getProgrammeCurrently: suspend (channelId: Int) -> Programme?,
    reloadThumbnail: suspend (channelUrl: String) -> Uri?,
    syncThumbnail: suspend (channelUrl: String) -> Uri?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val spacing = LocalSpacing.current
    val preferences = hiltPreferences()

    val actualRowCount = when {
        preferences.noPictureMode -> rowCount
        isVodOrSeriesPlaylist -> rowCount + 2
        else -> rowCount
    }

    val channels = categoryWithChannels?.channels?.collectAsLazyPagingItems()

    val currentGetProgrammeCurrently by rememberUpdatedState(getProgrammeCurrently)
    val currentReloadThumbnail by rememberUpdatedState(reloadThumbnail)
    val currentSyncThumbnail by rememberUpdatedState(syncThumbnail)

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(start = spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LazyVerticalStaggeredGrid(
            state = state,
            columns = StaggeredGridCells.Fixed(actualRowCount),
            verticalItemSpacing = spacing.medium,
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            contentPadding = PaddingValues(vertical = spacing.medium) + contentPadding,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            items(channels?.itemCount ?: 0) { index ->
                val channel = channels?.get(index)
                if (channel != null) {
                    val programme: Programme? by produceState<Programme?>(
                        initialValue = null,
                        key1 = channel.id
                    ) {
                        value = currentGetProgrammeCurrently(channel.id)
                    }
                    val loadedUrl: Any? by produceState<Any?>(initialValue = channel.cover) {
                        val default = channel.cover
                        delay(1200.milliseconds)
                        val channelUrl = channel.url
                        val reloaded = currentReloadThumbnail(channelUrl)
                        if (reloaded == null) {
                            value = currentSyncThumbnail(channelUrl) ?: default
                        } else {
                            value = reloaded
                            delay(2400.milliseconds)
                            value = currentSyncThumbnail(channelUrl) ?: default
                        }
                    }
                    ChannelItem(
                        channel = channel,
                        programme = programme,
                        cover = loadedUrl,
                        recently = recently,
                        zapping = zapping == channel,
                        isVodOrSeriesPlaylist = isVodOrSeriesPlaylist,
                        onClick = { onClick(channel) },
                        onLongClick = { onLongClick(channel) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
        VerticalDraggableScrollbar(
            lazyStaggeredGridState = state
        )
    }
}
