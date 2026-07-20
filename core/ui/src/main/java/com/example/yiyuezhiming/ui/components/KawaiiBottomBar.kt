package com.example.yiyuezhiming.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yiyuezhiming.navigation.Route
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.PaleRose
import com.example.yiyuezhiming.ui.theme.TextHint

data class BottomNavItem(
    val route: Route,
    val label: String,
    val icon: @Composable (selected: Boolean) -> Unit
)

@Composable
fun KawaiiBottomBar(
    currentRoute: String?,
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(Route.Toolbox, "百宝箱") { ToolboxIcon(Modifier.size(if (it) 30.dp else 28.dp)) },
        BottomNavItem(Route.Music, "音乐") { MusicCatIcon(Modifier.size(if (it) 30.dp else 28.dp)) },
        BottomNavItem(Route.Settings, "设置") { CatGearIcon(Modifier.size(if (it) 30.dp else 28.dp)) }
    )
    Row(
        modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .border(
                width = 1.dp,
                color = PaleRose,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = when (item.route) {
                Route.Toolbox -> currentRoute in setOf(
                    Route.Toolbox.path,
                    Route.Home.path,
                    Route.Reminders.path,
                    Route.Memo.path,
                    Route.Album.path,
                    Route.FortuneHub.path,
                    Route.DailySign.path,
                    Route.Tarot.path,
                    Route.NovelBookshelf.path,
                    Route.NovelReader.path
                )
                else -> currentRoute == item.route.path
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .kawaiiClickable(onClick = { onNavigate(item.route) })
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    item.icon(selected)
                    Text(
                        item.label,
                        color = if (selected) AccentHotPink else TextHint,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (selected) AccentHotPink else Color.Transparent)
                    )
                }
            }
        }
    }
}
