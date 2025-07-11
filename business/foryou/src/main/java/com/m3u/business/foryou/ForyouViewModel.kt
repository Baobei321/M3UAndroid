package com.m3u.business.foryou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.m3u.core.architecture.preferences.PreferencesKeys
import com.m3u.core.architecture.preferences.Settings
import com.m3u.core.architecture.preferences.flowOf
import com.m3u.core.wrapper.Resource
import com.m3u.core.wrapper.mapResource
import com.m3u.core.wrapper.resource
import com.m3u.data.database.model.Channel
import com.m3u.data.database.model.Playlist
import com.m3u.data.parser.xtream.XtreamChannelInfo
import com.m3u.data.repository.channel.ChannelRepository
import com.m3u.data.repository.playlist.PlaylistRepository
import com.m3u.data.repository.programme.ProgrammeRepository
import com.m3u.data.service.PlayerManager
import com.m3u.data.worker.SubscriptionWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@HiltViewModel
class ForyouViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    channelRepository: ChannelRepository,
    programmeRepository: ProgrammeRepository,
    private val playerManager: PlayerManager,
    settings: Settings,
    workManager: WorkManager,
) : ViewModel() {
    val playlists: StateFlow<Map<Playlist, Int>> = playlistRepository
        .observeAllCounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyMap()
        )

    val subscribingPlaylistUrls: StateFlow<List<String>> =
        workManager.getWorkInfosFlow(
            WorkQuery.fromStates(
                WorkInfo.State.RUNNING,
                WorkInfo.State.ENQUEUED,
            )
        )
            .combine(playlistRepository.observePlaylistUrls()) { infos, playlistUrls ->
                infos
                    .filter { info -> SubscriptionWorker.TAG in info.tags }
                    .mapNotNull { info -> info.tags.find { it in playlistUrls } }
            }
            .stateIn(
                scope = viewModelScope,
                initialValue = emptyList(),
                started = SharingStarted.WhileSubscribed(5_000L)
            )

    val refreshingEpgUrls: Flow<List<String>> = programmeRepository.refreshingEpgUrls

    private val unseensDuration = settings.flowOf(PreferencesKeys.UNSEENS_MILLISECONDS)
        .map { it.toDuration(DurationUnit.MILLISECONDS) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = Duration.INFINITE
        )

    val specs = combine(
        unseensDuration.flatMapLatest { channelRepository.observeAllUnseenFavorites(it) },
        channelRepository.observePlayedRecently(),
    ) { channels, playedRecently ->
        listOfNotNull<Recommend.Spec>(
            playedRecently?.let { Recommend.CwSpec(it, playerManager.getCwPosition(it.url)) },
            *(channels.map { channel -> Recommend.UnseenSpec(channel) }.take(8).toTypedArray())
        )
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(1_000L),
            initialValue = emptyList()
        )

    fun onUnsubscribePlaylist(url: String) {
        viewModelScope.launch {
            playlistRepository.unsubscribe(url)
        }
    }

    val series = MutableStateFlow<Channel?>(null)
    val seriesReplay = MutableStateFlow(0)
    val episodes: StateFlow<Resource<List<XtreamChannelInfo.Episode>>> = series
        .combine(seriesReplay) { series, _ -> series }
        .flatMapLatest { series ->
            if (series == null) flow { }
            else resource { playlistRepository.readEpisodesOrThrow(series) }
                .mapResource { it }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = Resource.Loading,
            // don't lose
            started = SharingStarted.Lazily
        )

    val query = MutableStateFlow<String>("")

    suspend fun getPlaylist(playlistUrl: String): Playlist? =
        playlistRepository.get(playlistUrl)
}
