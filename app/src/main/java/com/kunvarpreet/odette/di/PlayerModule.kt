package com.kunvarpreet.odette.di

import android.content.Context
import com.kunvarpreet.odette.data.repository.UserPreferencesRepository
import com.kunvarpreet.odette.domain.usecase.RecordPlaybackUseCase
import com.kunvarpreet.odette.player.MusicPlayerController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun provideMusicPlayerController(
        @ApplicationContext context: Context,
        recordPlaybackUseCase: RecordPlaybackUseCase,
        userPreferencesRepository: UserPreferencesRepository
    ): MusicPlayerController {
        return MusicPlayerController(context, recordPlaybackUseCase, userPreferencesRepository)
    }
}
