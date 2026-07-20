package com.example.yiyuezhiming.ui.theme

import androidx.compose.ui.graphics.Color

// ─── 主色系：玫瑰粉（降饱和、增灰调，更高级） ───
val PrimaryPink = Color(0xFFF2A7BB)       // 柔玫瑰粉（主色）
val SecondaryPink = Color(0xFFF7C4D4)     // 浅樱粉（辅助）
val AccentHotPink = Color(0xFFE8789A)     // 深玫瑰（强调/按钮）
val DeepRose = Color(0xFFC4567A)          // 暗玫瑰（ pressed 态/标题）

// ─── 背景与表面：奶白微暖灰，减少视觉疲劳 ───
val BackgroundPink = Color(0xFFFBF6F7)    // 页面背景（暖灰粉白）
val SoftBlush = Color(0xFFFFF9FA)         // 卡片表面
val CloudWhite = Color(0xFFFDF8F4)        // 次级表面（微暖）
val CreamPink = Color(0xFFF9E8EC)         // 高亮区域/选中背景
val PaleRose = Color(0xFFF5DDE4)          // 分割线/边框

// ─── 功能色：各模块渐变色对 ───
val LavenderMist = Color(0xFFE8DFF5)      // 运势/紫色系
val PeachGlow = Color(0xFFFFE4D6)         // 日期/暖橙系
val MintWhisper = Color(0xFFE3F5EE)       // 备忘/薄荷系
val SkyBlush = Color(0xFFE0F0FA)          // 相册/天蓝系
val HoneyCream = Color(0xFFFFF3DC)        // 小说/蜂蜜系

// ─── 文字色 ───
val TextBrown = Color(0xFF4A3738)         // 主文字（深棕粉，比纯黑温柔）
val TextSecondary = Color(0xFF8C7274)     // 次级文字（灰粉）
val TextHint = Color(0xFFB8A3A5)          // 提示文字

// ─── 暗色模式：温暖深棕粉调 ───
val DarkBackground = Color(0xFF2A2226)    // 深棕黑
val DarkSurface = Color(0xFF3A3035)       // 卡片表面
val DarkSurfaceVariant = Color(0xFF4A3D42)// 次级表面
val DarkText = Color(0xFFF5E6E8)          // 主文字
val DarkTextSecondary = Color(0xFFC4AEB0) // 次级文字
val DarkAccent = Color(0xFFD4849A)        // 强调色（暗玫瑰）
val DarkWine = Color(0xFF5E3B4D)          // 装饰/边框

// ─── 渐变预设 ───
val GradientPink = listOf(Color(0xFFF7C4D4), Color(0xFFE8789A))
val GradientLavender = listOf(Color(0xFFE8DFF5), Color(0xFFB8A3D4))
val GradientPeach = listOf(Color(0xFFFFE4D6), Color(0xFFF2A7BB))
val GradientMint = listOf(Color(0xFFE3F5EE), Color(0xFFA8D8C8))
