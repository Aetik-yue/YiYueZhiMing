package com.example.yiyuezhiming.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.yiyuezhiming.data.local.AiChatDao
import com.example.yiyuezhiming.data.local.AlbumDao
import com.example.yiyuezhiming.data.local.DeepSeekLogDao
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
    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS ai_chat_messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    role TEXT NOT NULL,
                    content TEXT NOT NULL,
                    status TEXT NOT NULL,
                    errorMessage TEXT,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS fortune_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    type TEXT NOT NULL,
                    date TEXT NOT NULL,
                    drawTitle TEXT NOT NULL,
                    drawSubtitle TEXT NOT NULL,
                    drawContent TEXT NOT NULL,
                    keywords TEXT NOT NULL,
                    interpretation TEXT NOT NULL,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_fortune_records_type_date ON fortune_records(type, date)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS deepseek_request_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    module TEXT NOT NULL,
                    startedAt INTEGER NOT NULL,
                    durationMs INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    errorSummary TEXT
                )
                """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): YiYueDatabase =
        Room.databaseBuilder(context, YiYueDatabase::class.java, "yi_yue.db")
            .addMigrations(migration2To3)
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

    @Provides
    fun provideAiChatDao(database: YiYueDatabase): AiChatDao = database.aiChatDao()

    @Provides
    fun provideFortuneDao(database: YiYueDatabase): FortuneDao = database.fortuneDao()

    @Provides
    fun provideDeepSeekLogDao(database: YiYueDatabase): DeepSeekLogDao = database.deepSeekLogDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
