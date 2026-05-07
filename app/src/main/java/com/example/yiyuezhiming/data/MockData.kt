package com.example.yiyuezhiming.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.yiyuezhiming.model.AnimalFace
import com.example.yiyuezhiming.model.Memory
import com.example.yiyuezhiming.model.Mood
import com.example.yiyuezhiming.model.Reminder
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CreamPink
import com.example.yiyuezhiming.ui.theme.LavenderMist
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.SecondaryPink
import java.time.LocalDate

object MockData {
    val moods = listOf(
        Mood("开心", AnimalFace.BunnyHappy, PrimaryPink),
        Mood("甜甜", AnimalFace.CatSmile, SecondaryPink),
        Mood("平静", AnimalFace.BearCalm, CreamPink),
        Mood("想抱抱", AnimalFace.CatSad, LavenderMist),
        Mood("小生气", AnimalFace.BunnyPout, Color(0xFFFFD1DC)),
        Mood("感动", AnimalFace.BearTear, Color(0xFFD9E9FF))
    )

    val memories = listOf(
        Memory(
            id = 1,
            date = LocalDate.of(2026, 5, 20),
            mood = moods[0],
            note = "今天一起去喝了草莓奶昔，你把第一口让给我的时候，我偷偷开心了很久。",
            songTitle = "简单爱",
            artistName = "周杰伦",
            imageColorArgb = Color(0xFFFFC8DD).toArgb(),
            category = "甜蜜"
        ),
        Memory(
            id = 2,
            date = LocalDate.of(2026, 5, 12),
            mood = moods[1],
            note = "傍晚散步的时候风很温柔，你也是。",
            songTitle = "Perfect",
            artistName = "Ed Sheeran",
            imageColorArgb = Color(0xFFFFD6A5).toArgb(),
            category = "约会"
        ),
        Memory(
            id = 3,
            date = LocalDate.of(2026, 4, 30),
            mood = moods[2],
            note = "我们一起听了那首歌，像把整个晚上都收藏起来了。",
            songTitle = "告白气球",
            artistName = "周杰伦",
            imageColorArgb = Color(0xFFCDB4DB).toArgb(),
            category = "日常"
        ),
        Memory(
            id = 4,
            date = LocalDate.of(2026, 4, 8),
            mood = moods[5],
            note = "下雨天窝在一起看电影，连窗外的雨声都变甜了。",
            songTitle = "Love Story",
            artistName = "Taylor Swift",
            imageColorArgb = Color(0xFFBDE0FE).toArgb(),
            category = "纪念日"
        )
    )

    val reminders = listOf(
        Reminder(1, "恋爱100天", "纪念日", LocalDate.of(2026, 5, 20), true),
        Reminder(2, "第一次见面的日子", "初见", LocalDate.of(2026, 6, 2), true),
        Reminder(3, "她的生日", "生日", LocalDate.of(2026, 8, 16), true),
        Reminder(4, "第一次旅行纪念日", "旅行", LocalDate.of(2026, 10, 3), false)
    )

    val albumFilters = listOf("全部", "甜蜜", "约会", "旅行", "日常", "纪念日", "最近7天", "本月")
}
