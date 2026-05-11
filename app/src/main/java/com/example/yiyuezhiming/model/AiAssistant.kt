package com.example.yiyuezhiming.model

data class AiAssistant(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val prompt: String,
    val isPreset: Boolean = false,
    val webSearchEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
