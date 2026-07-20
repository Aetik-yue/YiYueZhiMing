package com.example.yiyuezhiming.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "album_photos")
data class AlbumPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val uri: String,
    val memoryTag: String,
    val takenDate: LocalDate,
    val createdAt: Long,
    val updatedAt: Long
)

