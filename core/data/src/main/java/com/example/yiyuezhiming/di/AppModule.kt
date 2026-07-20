package com.example.yiyuezhiming.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.yiyuezhiming.data.local.AlbumDao
import com.example.yiyuezhiming.data.local.BookDao
import com.example.yiyuezhiming.data.local.FortuneDao
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
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): YiYueDatabase =
        Room.databaseBuilder(context, YiYueDatabase::class.java, "yi_yue.db")
            .addMigrations(MIGRATION_7_8)
            .fallbackToDestructiveMigration()
            .build()

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE books ADD COLUMN totalPages INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE books ADD COLUMN currentPageInBook INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides
    fun provideMemoryDao(database: YiYueDatabase): MemoryDao = database.memoryDao()

    @Provides
    fun provideReminderDao(database: YiYueDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideMemoDao(database: YiYueDatabase): MemoDao = database.memoDao()

    @Provides
    fun provideAlbumDao(database: YiYueDatabase): AlbumDao = database.albumDao()

    @Provides
    fun provideFortuneDao(database: YiYueDatabase): FortuneDao = database.fortuneDao()

    @Provides
    fun provideBookDao(database: YiYueDatabase): BookDao = database.bookDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
