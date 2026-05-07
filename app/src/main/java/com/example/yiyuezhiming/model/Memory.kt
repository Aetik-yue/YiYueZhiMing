package com.example.yiyuezhiming.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.time.LocalDate

data class Memory(
    val id: Long = 0,
    val date: LocalDate,
    val mood: Mood,
    val note: String,
    val songTitle: String = "",
    val artistName: String = "",
    val musicUri: String? = null,
    val photoUri: String? = null,
    val imageColorArgb: Int = Color(0xFFFFC8DD).toArgb(),
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    val imageColor: Color get() = Color(imageColorArgb)
    val dateText: String get() = "%04d.%02d.%02d".format(date.year, date.monthValue, date.dayOfMonth)
}

data class Mood(
    val label: String,
    val face: AnimalFace,
    val color: Color
)

enum class AnimalFace {
    BunnyHappy,
    CatSmile,
    BearCalm,
    CatSad,
    BunnyPout,
    BearTear
}
