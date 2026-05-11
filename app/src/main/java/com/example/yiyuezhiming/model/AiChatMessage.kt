package com.example.yiyuezhiming.model

data class AiChatMessage(
    val id: Long = 0,
    val sessionId: Long = 1,
    val role: String,
    val content: String,
    val status: String,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
