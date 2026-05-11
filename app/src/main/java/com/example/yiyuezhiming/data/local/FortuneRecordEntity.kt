package com.example.yiyuezhiming.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "fortune_records",
    indices = [Index(value = ["type", "date"], unique = true)]
)
data class FortuneRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val date: LocalDate,
    val drawTitle: String,
    val drawSubtitle: String,
    val drawContent: String,
    val keywords: String,
    val interpretation: String,
    val createdAt: Long = System.currentTimeMillis()
)

