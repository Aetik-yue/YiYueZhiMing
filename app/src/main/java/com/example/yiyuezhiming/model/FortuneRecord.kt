package com.example.yiyuezhiming.model

import java.time.LocalDate

data class FortuneRecord(
    val id: Long = 0,
    val type: String,
    val date: LocalDate,
    val drawTitle: String,
    val drawSubtitle: String,
    val drawContent: String,
    val keywords: String,
    val interpretation: String,
    val createdAt: Long = System.currentTimeMillis()
)

