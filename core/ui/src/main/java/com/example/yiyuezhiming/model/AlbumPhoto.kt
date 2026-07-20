package com.example.yiyuezhiming.model

import java.time.LocalDate

data class AlbumPhoto(
    val id: Long = 0,
    val category: String,
    val uri: String,
    val memoryTag: String = "",
    val takenDate: LocalDate,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val dateText: String get() = "%04d.%02d.%02d".format(takenDate.year, takenDate.monthValue, takenDate.dayOfMonth)
}

