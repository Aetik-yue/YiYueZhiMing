package com.example.yiyuezhiming.navigation

sealed class Route(val path: String) {
    data object Splash : Route("splash")
    data object Home : Route("home")
    data object AddMemory : Route("add-memory")
    data object Memo : Route("memo")
    data object Album : Route("album")
    data object Reminders : Route("reminders")
    data object FortuneHub : Route("fortune")
    data object DailySign : Route("daily-sign")
    data object Tarot : Route("tarot")
    data object NovelBookshelf : Route("novel-bookshelf")
    data object NovelReader : Route("novel-reader/{bookId}") {
        fun create(bookId: String): String = "novel-reader/$bookId"
    }
    data object AiChat : Route("ai-chat")
    data object Toolbox : Route("toolbox")
    data object Music : Route("music")
    data object Settings : Route("settings")
}
