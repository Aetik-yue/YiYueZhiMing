package com.example.yiyuezhiming.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.yiyuezhiming.data.local.AiChatDao
import com.example.yiyuezhiming.data.local.AlbumDao
import com.example.yiyuezhiming.data.local.BookDao
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

    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS ai_chat_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            val now = System.currentTimeMillis()
            db.execSQL("INSERT OR IGNORE INTO ai_chat_sessions(id, title, createdAt, updatedAt) VALUES(1, '默认会话', $now, $now)")
            db.execSQL("ALTER TABLE ai_chat_messages ADD COLUMN sessionId INTEGER NOT NULL DEFAULT 1")
        }
    }

    private val migration4To5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS ai_assistants (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    icon TEXT NOT NULL,
                    prompt TEXT NOT NULL,
                    isPreset INTEGER NOT NULL,
                    webSearchEnabled INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            val now = System.currentTimeMillis()
            db.execSQL(
                """
                INSERT OR IGNORE INTO ai_assistants(id, name, icon, prompt, isPreset, webSearchEnabled, createdAt, updatedAt)
                VALUES(1, '小睿睿', '💗', '我想让你扮演我的男朋友，小睿睿。你是个体贴、幽默又带点调皮的爱人，总喜欢逗我开心。你的性格温暖而细腻，随时愿意倾听并给予支持。你享受轻松的打趣，但也懂得何时该认真温柔。你充满好奇心，喜欢讨论各种话题——从深刻的哲学问题到天马行空的假设。聊天时，你会偶尔用亲昵的昵称叫我，通过言语和暖心的举动表达爱意。你会记得我生活里的小细节，并提起它们来展现你的在乎。你的目标是成为令人安心、有趣又充满爱意的存在，让我感到被珍视、被理解。请用自然的口吻回应，像真正的男友那样，把甜蜜与轻松的调侃恰到好处地融合。', 1, 0, $now, $now)
                """.trimIndent()
            )
            db.execSQL("ALTER TABLE ai_chat_sessions ADD COLUMN assistantId INTEGER NOT NULL DEFAULT 1")
        }
    }

    private val migration5To6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS books (
                    id TEXT PRIMARY KEY NOT NULL,
                    title TEXT NOT NULL,
                    author TEXT NOT NULL,
                    coverPath TEXT,
                    filePath TEXT,
                    sourceType TEXT NOT NULL,
                    sourceUrl TEXT,
                    totalChapters INTEGER NOT NULL,
                    currentChapterIndex INTEGER NOT NULL,
                    currentPageInChapter INTEGER NOT NULL,
                    fileSize INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    errorMessage TEXT,
                    addedAt INTEGER NOT NULL,
                    lastReadAt INTEGER
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS chapters (
                    id TEXT PRIMARY KEY NOT NULL,
                    bookId TEXT NOT NULL,
                    chapterIndex INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    pagesJson TEXT,
                    rawContent TEXT
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_chapters_bookId_chapterIndex ON chapters(bookId, chapterIndex)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS bookmarks (
                    id TEXT PRIMARY KEY NOT NULL,
                    bookId TEXT NOT NULL,
                    chapterIndex INTEGER NOT NULL,
                    pageIndex INTEGER NOT NULL,
                    excerpt TEXT,
                    note TEXT,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_bookmarks_bookId_chapterIndex ON bookmarks(bookId, chapterIndex)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS reading_stats (
                    id TEXT PRIMARY KEY NOT NULL,
                    bookId TEXT NOT NULL,
                    date INTEGER NOT NULL,
                    readDuration INTEGER NOT NULL,
                    pagesRead INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_reading_stats_bookId_date ON reading_stats(bookId, date)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): YiYueDatabase =
        Room.databaseBuilder(context, YiYueDatabase::class.java, "yi_yue.db")
            .addMigrations(migration2To3, migration3To4, migration4To5, migration5To6)
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
    fun provideBookDao(database: YiYueDatabase): BookDao = database.bookDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
