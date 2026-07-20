package com.example.yiyuezhiming.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val coverPath: String?,
    val filePath: String?,
    val sourceType: String,
    val sourceUrl: String?,
    val totalChapters: Int,
    val currentChapterIndex: Int = 0,
    val currentPageInChapter: Int = 0,
    val fileSize: Long = 0,
    val status: String = "READY",
    val errorMessage: String? = null,
    val addedAt: Long,
    val lastReadAt: Long?
)
