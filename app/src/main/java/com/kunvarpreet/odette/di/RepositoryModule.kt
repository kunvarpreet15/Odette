package com.kunvarpreet.odette.di

import com.kunvarpreet.odette.data.repository.FavoritesRepositoryImpl
import com.kunvarpreet.odette.data.repository.HistoryRepositoryImpl
import com.kunvarpreet.odette.data.repository.MusicRepositoryImpl
import com.kunvarpreet.odette.data.repository.PlaylistRepositoryImpl
import com.kunvarpreet.odette.domain.repository.FavoritesRepository
import com.kunvarpreet.odette.domain.repository.HistoryRepository
import com.kunvarpreet.odette.domain.repository.MusicRepository
import com.kunvarpreet.odette.domain.repository.PlaylistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        impl: MusicRepositoryImpl
    ): MusicRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(
        impl: FavoritesRepositoryImpl
    ): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        impl: PlaylistRepositoryImpl
    ): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        impl: HistoryRepositoryImpl
    ): HistoryRepository
}
