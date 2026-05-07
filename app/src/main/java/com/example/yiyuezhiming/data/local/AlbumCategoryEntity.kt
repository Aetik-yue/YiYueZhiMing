package com.example.yiyuezhiming.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "album_categories")
data class AlbumCategoryEntity(
    @PrimaryKey
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

