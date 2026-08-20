package com.kunvarpreet.odette.di

import android.content.Context
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
        @ApplicationContext context: Context
    ): MusicPlayerController {
        return MusicPlayerController(context)
    }
}
