package com.example.yiyuezhiming.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_stats",
    indices = [Index(value = ["bookId", "date"])]
)
data class ReadingStatEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val date: Long,
    val readDuration: Long,
    val pagesRead: Int
)
