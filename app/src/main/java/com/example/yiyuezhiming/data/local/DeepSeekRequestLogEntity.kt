package com.example.yiyuezhiming.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deepseek_request_logs")
data class DeepSeekRequestLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val module: String,
    val startedAt: Long,
    val durationMs: Long,
    val status: String,
    val errorSummary: String? = null
)

