package com.m3u.data

import com.google.auto.service.AutoService
import com.m3u.core.util.context.ContextUtils
import com.m3u.data.database.dao.ChannelDao
import com.m3u.data.database.dao.PlaylistDao
import com.m3u.extension.api.RemoteServiceDependencies
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@AutoService(RemoteServiceDependencies::class)
class RemoteServiceDependenciesImpl : RemoteServiceDependencies {
    @dagger.hilt.EntryPoint
    @InstallIn(SingletonComponent::class)
    interface EntryPoint {
        val playlistDao: PlaylistDao
        val channelDao: ChannelDao
    }

    private val entryPoint: EntryPoint by lazy {
        EntryPointAccessors.fromApplication(
            ContextUtils.getContext(),
            EntryPoint::class.java
        )
    }

    override val playlistDao: Any = entryPoint.playlistDao
    override val channelDao: Any = entryPoint.channelDao
}