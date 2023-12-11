@file:Suppress("unused")

package com.m3u.core.architecture.pref.di

import com.m3u.core.architecture.pref.Pref
import com.m3u.core.architecture.pref.SnapshotPref
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ConfigurationModule {
    @Binds
    @Singleton
    fun bindPref(pref: SnapshotPref): Pref
}
