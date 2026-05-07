package com.example.yiyuezhiming.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val moodLabel: String,
    val moodFace: String,
    val note: String,
    val songTitle: String,
    val artistName: String,
    val musicUri: String?,
    val photoUri: String?,
    val imageColorArgb: Int,
    val category: String,
    val createdAt: Long
)
