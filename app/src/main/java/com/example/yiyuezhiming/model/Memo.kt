package com.example.yiyuezhiming.model

data class Memo(
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String,
    val isPinned: Boolean = false,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

