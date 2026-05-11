package com.example.yiyuezhiming.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MemoryEntity::class,
        ReminderEntity::class,
        MemoEntity::class,
        MemoCategoryEntity::class,
        AlbumPhotoEntity::class,
        AlbumCategoryEntity::class,
        AiChatMessageEntity::class,
        FortuneRecordEntity::class,
        DeepSeekRequestLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class YiYueDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun memoDao(): MemoDao
    abstract fun albumDao(): AlbumDao
    abstract fun aiChatDao(): AiChatDao
    abstract fun fortuneDao(): FortuneDao
    abstract fun deepSeekLogDao(): DeepSeekLogDao
}
