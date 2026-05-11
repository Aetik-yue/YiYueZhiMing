package com.example.yiyuezhiming.model

data class AiChatSession(
    val id: Long = 0,
    val assistantId: Long = 1,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
