package com.example.yiyuezhiming.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_chat_messages")
data class AiChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long = 1,
    val role: String,
    val content: String,
    val status: String,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
