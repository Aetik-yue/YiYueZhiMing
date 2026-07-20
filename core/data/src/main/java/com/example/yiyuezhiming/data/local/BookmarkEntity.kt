package com.example.yiyuezhiming.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["bookId", "chapterIndex"])]
)
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val chapterIndex: Int,
    val pageIndex: Int,
    val excerpt: String?,
    val note: String?,
    val createdAt: Long
)
