package com.example.yiyuezhiming.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memo_categories")
data class MemoCategoryEntity(
    @PrimaryKey
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

