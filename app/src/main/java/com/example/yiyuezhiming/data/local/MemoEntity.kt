package com.example.yiyuezhiming.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memos")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String,
    val isPinned: Boolean,
    val isDone: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

