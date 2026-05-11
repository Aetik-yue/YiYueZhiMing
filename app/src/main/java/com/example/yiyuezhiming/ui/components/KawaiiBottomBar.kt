package com.example.yiyuezhiming.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.navigation.Route
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink

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
        BottomNavItem(Route.AiChat, "AI") { AppLogoIcon(Modifier.size(if (it) 30.dp else 28.dp)) },
        BottomNavItem(Route.Music, "音乐") { MusicCatIcon(Modifier.size(if (it) 30.dp else 28.dp)) },
        BottomNavItem(Route.Settings, "设置") { CatGearIcon(Modifier.size(if (it) 30.dp else 28.dp)) }
    )
    Row(
        modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                        CloudWhite.copy(alpha = 0.96f)
                    )
                ),
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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
                    Route.Tarot.path
                )
                else -> currentRoute == item.route.path
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        if (selected) Brush.horizontalGradient(listOf(PrimaryPink, AccentHotPink))
                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)),
                        RoundedCornerShape(22.dp)
                    )
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
                        color = if (selected) Color.White else AccentHotPink,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}

