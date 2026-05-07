package com.example.yiyuezhiming.navigation

sealed class Route(val path: String) {
    data object Splash : Route("splash")
    data object Home : Route("home")
    data object AddMemory : Route("add-memory")
    data object Memo : Route("memo")
    data object Album : Route("album")
    data object Reminders : Route("reminders")
    data object Toolbox : Route("toolbox")
    data object Music : Route("music")
    data object Settings : Route("settings")
}
