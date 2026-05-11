package com.example.yiyuezhiming.model

data class DeepSeekRequestLog(
    val id: Long = 0,
    val module: String,
    val startedAt: Long,
    val durationMs: Long,
    val status: String,
    val errorSummary: String? = null
)

