package com.example.yiyuezhiming.ui.screens.toolbox

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.drawHeart
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.components.KawaiiCalendarIcon
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.components.PixelSamoyedIcon
import com.example.yiyuezhiming.ui.components.RabbitLogo
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.LavenderMist
import com.example.yiyuezhiming.ui.theme.PrimaryPink

private data class ToolboxItem(
    val title: String,
    val subtitle: String,
    val colors: List<Color>,
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit
)

@Composable
fun ToolboxScreen(
    onOpenMemories: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenMemo: () -> Unit,
    onOpenAlbum: () -> Unit,
    onOpenFortune: () -> Unit,
    onOpenNovel: () -> Unit
) {
    val items = listOf(
        ToolboxItem(
            title = "点滴",
            subtitle = "记录此刻心动",
            colors = listOf(Color.White, CloudWhite, PrimaryPink.copy(alpha = 0.34f)),
            icon = { RabbitLogo(Modifier.fillMaxSize().padding(16.dp)) },
            onClick = onOpenMemories
        ),
        ToolboxItem(
            title = "日期",
            subtitle = "收藏重要日子",
            colors = listOf(Color.White, Color(0xFFFFD6A5).copy(alpha = 0.58f), LavenderMist.copy(alpha = 0.72f)),
            icon = { KawaiiCalendarIcon(Modifier.fillMaxSize().padding(26.dp)) },
            onClick = onOpenReminders
        ),
        ToolboxItem(
            title = "备忘录",
            subtitle = "记下温柔小事",
            colors = listOf(Color.White, CloudWhite, Color(0xFFFFE4F0)),
            icon = { MemoIcon(Modifier.fillMaxSize().padding(24.dp)) },
            onClick = onOpenMemo
        ),
        ToolboxItem(
            title = "相册",
            subtitle = "收藏我们的照片",
            colors = listOf(Color.White, CloudWhite, Color(0xFFFFD6E8)),
            icon = { PixelSamoyedIcon(Modifier.fillMaxSize().padding(24.dp)) },
            onClick = onOpenAlbum
        ),
        ToolboxItem(
            title = "运势",
            subtitle = "签运和塔罗指引",
            colors = listOf(Color.White, LavenderMist.copy(alpha = 0.82f), PrimaryPink.copy(alpha = 0.32f)),
            icon = { FortuneIcon(Modifier.fillMaxSize().padding(24.dp)) },
            onClick = onOpenFortune
        ),
        ToolboxItem(
            title = "小说",
            subtitle = "私人阅读小书架",
            colors = listOf(Color.White, CloudWhite, Color(0xFFFFF1C7)),
            icon = { NovelIcon(Modifier.fillMaxSize().padding(24.dp)) },
            onClick = onOpenNovel
        )
    )

    AnimatedCloudBackground {
        Column(Modifier.fillMaxSize()) {
            KawaiiTopBar(title = "百宝箱", showLogo = true)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(items) { item ->
                    ToolboxCard(item)
                }
                item { Spacer(Modifier.height(86.dp)) }
            }
        }
    }
}

@Composable
private fun MemoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawRoundRect(
            color = CloudWhite,
            topLeft = Offset(size.width * 0.18f, size.height * 0.1f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.78f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
        )
        repeat(4) { index ->
            val y = size.height * (0.28f + index * 0.13f)
            drawLine(
                color = AccentHotPink.copy(alpha = 0.55f),
                start = Offset(size.width * 0.3f, y),
                end = Offset(size.width * 0.7f, y),
                strokeWidth = 5f
            )
        }
        drawCircle(PrimaryPink, radius = size.minDimension * 0.08f, center = Offset(size.width * 0.72f, size.height * 0.18f))
    }
}

@Composable
private fun FortuneIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawCircle(CloudWhite, radius = size.minDimension * 0.34f, center = Offset(size.width * 0.5f, size.height * 0.5f))
        drawCircle(
            AccentHotPink.copy(alpha = 0.28f),
            radius = size.minDimension * 0.22f,
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            style = Stroke(width = 8f)
        )
        drawLine(AccentHotPink, Offset(size.width * 0.5f, size.height * 0.26f), Offset(size.width * 0.5f, size.height * 0.74f), 6f)
        drawLine(AccentHotPink, Offset(size.width * 0.26f, size.height * 0.5f), Offset(size.width * 0.74f, size.height * 0.5f), 6f)
        drawHeart(Offset(size.width * 0.72f, size.height * 0.28f), 10f, PrimaryPink)
    }
}

@Composable
private fun NovelIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawRoundRect(
            color = CloudWhite,
            topLeft = Offset(size.width * 0.22f, size.height * 0.14f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.56f, size.height * 0.72f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
        )
        drawLine(
            color = AccentHotPink.copy(alpha = 0.7f),
            start = Offset(size.width * 0.5f, size.height * 0.18f),
            end = Offset(size.width * 0.5f, size.height * 0.82f),
            strokeWidth = 5f
        )
        repeat(4) { index ->
            val y = size.height * (0.3f + index * 0.11f)
            drawLine(
                color = PrimaryPink.copy(alpha = 0.65f),
                start = Offset(size.width * 0.32f, y),
                end = Offset(size.width * 0.44f, y),
                strokeWidth = 4f
            )
            drawLine(
                color = PrimaryPink.copy(alpha = 0.65f),
                start = Offset(size.width * 0.56f, y),
                end = Offset(size.width * 0.68f, y),
                strokeWidth = 4f
            )
        }
        drawHeart(Offset(size.width * 0.72f, size.height * 0.24f), 9f, AccentHotPink)
    }
}

@Composable
private fun ToolboxCard(item: ToolboxItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = PrimaryPink.copy(alpha = 0.18f),
                spotColor = AccentHotPink.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(item.colors))
            .kawaiiClickable(pressedScale = 0.95f, onClick = item.onClick)
            .padding(14.dp)
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                color = AccentHotPink.copy(alpha = 0.12f),
                radius = size.minDimension * 0.26f,
                center = Offset(size.width * 0.82f, size.height * 0.18f),
                style = Stroke(width = 5f)
            )
            drawHeart(
                center = Offset(size.width * 0.16f, size.height * 0.2f),
                size = 8f,
                color = AccentHotPink.copy(alpha = 0.5f)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth(0.48f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            item.icon()
        }
        Column(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                item.title,
                color = AccentHotPink,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                item.subtitle,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
