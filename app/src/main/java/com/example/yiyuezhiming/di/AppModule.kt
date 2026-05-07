package com.example.yiyuezhiming.di

import android.content.Context
import androidx.room.Room
import com.example.yiyuezhiming.data.local.AlbumDao
import com.example.yiyuezhiming.data.local.MemoryDao
import com.example.yiyuezhiming.data.local.MemoDao
import com.example.yiyuezhiming.data.local.ReminderDao
import com.example.yiyuezhiming.data.local.YiYueDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): YiYueDatabase =
        Room.databaseBuilder(context, YiYueDatabase::class.java, "yi_yue.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMemoryDao(database: YiYueDatabase): MemoryDao = database.memoryDao()

    @Provides
    fun provideReminderDao(database: YiYueDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideMemoDao(database: YiYueDatabase): MemoDao = database.memoDao()

    @Provides
    fun provideAlbumDao(database: YiYueDatabase): AlbumDao = database.albumDao()
}
