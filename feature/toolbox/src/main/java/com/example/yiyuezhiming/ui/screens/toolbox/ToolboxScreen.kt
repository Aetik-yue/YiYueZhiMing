package com.example.yiyuezhiming.ui.screens.toolbox

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.components.KawaiiCalendarIcon
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.components.PixelSamoyedIcon
import com.example.yiyuezhiming.ui.components.RabbitLogo
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CreamPink
import com.example.yiyuezhiming.ui.theme.HoneyCream
import com.example.yiyuezhiming.ui.theme.LavenderMist
import com.example.yiyuezhiming.ui.theme.MintWhisper
import com.example.yiyuezhiming.ui.theme.PeachGlow
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.SkyBlush
import com.example.yiyuezhiming.ui.theme.SoftBlush
import com.example.yiyuezhiming.ui.theme.TextSecondary

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
            colors = listOf(SoftBlush, CreamPink, PrimaryPink.copy(alpha = 0.2f)),
            icon = { RabbitLogo(Modifier.fillMaxSize().padding(16.dp)) },
            onClick = onOpenMemories
        ),
        ToolboxItem(
            title = "日期",
            subtitle = "收藏重要日子",
            colors = listOf(SoftBlush, PeachGlow, PrimaryPink.copy(alpha = 0.15f)),
            icon = { KawaiiCalendarIcon(Modifier.fillMaxSize().padding(26.dp)) },
            onClick = onOpenReminders
        ),
        ToolboxItem(
            title = "备忘录",
            subtitle = "记下温柔小事",
            colors = listOf(SoftBlush, MintWhisper, AccentHotPink.copy(alpha = 0.1f)),
            icon = { MemoIcon(Modifier.fillMaxSize().padding(24.dp)) },
            onClick = onOpenMemo
        ),
        ToolboxItem(
            title = "相册",
            subtitle = "收藏我们的照片",
            colors = listOf(SoftBlush, SkyBlush, PrimaryPink.copy(alpha = 0.15f)),
            icon = { PixelSamoyedIcon(Modifier.fillMaxSize().padding(24.dp)) },
            onClick = onOpenAlbum
        ),
        ToolboxItem(
            title = "运势",
            subtitle = "签运和塔罗指引",
            colors = listOf(SoftBlush, LavenderMist, AccentHotPink.copy(alpha = 0.12f)),
            icon = { FortuneIcon(Modifier.fillMaxSize().padding(24.dp)) },
            onClick = onOpenFortune
        ),
        ToolboxItem(
            title = "小说",
            subtitle = "私人阅读小书架",
            colors = listOf(SoftBlush, HoneyCream, PrimaryPink.copy(alpha = 0.15f)),
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
        val w = size.width
        val h = size.height
        val padX = w * 0.15f
        val padY = h * 0.15f
        val contentW = w * 0.70f
        val contentH = h * 0.70f
        val cornerR = minOf(contentW, contentH) * 0.25f

        // Rounded notepad body (MintWhisper fill)
        drawRoundRect(
            color = MintWhisper,
            topLeft = Offset(padX, padY),
            size = Size(contentW, contentH),
            cornerRadius = CornerRadius(cornerR)
        )

        // 3 horizontal lines (AccentHotPink, 0.5 alpha)
        repeat(3) { index ->
            val lineY = padY + contentH * (0.30f + index * 0.20f)
            drawLine(
                color = AccentHotPink.copy(alpha = 0.5f),
                start = Offset(padX + contentW * 0.15f, lineY),
                end = Offset(padX + contentW * 0.75f, lineY),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )
        }

        // Small pencil accent (top-right corner area)
        val pencilStart = Offset(padX + contentW * 0.78f, padY + contentH * 0.08f)
        val pencilEnd = Offset(padX + contentW * 0.92f, padY + contentH * 0.28f)
        drawLine(PrimaryPink, pencilStart, pencilEnd, 5f, StrokeCap.Round)
        // Pencil tip
        drawCircle(AccentHotPink, 3f, pencilEnd)
    }
}

@Composable
private fun FortuneIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val c = Offset(w * 0.5f, h * 0.5f)
        val radius = size.minDimension * 0.35f

        // Crystal ball circle (LavenderMist fill)
        drawCircle(LavenderMist, radius, c)

        // 4-point star inside (AccentHotPink)
        val starR = radius * 0.55f
        val starInner = radius * 0.18f
        // Vertical line of star
        drawLine(
            color = AccentHotPink,
            start = Offset(c.x, c.y - starR),
            end = Offset(c.x, c.y + starR),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
        // Horizontal line of star
        drawLine(
            color = AccentHotPink,
            start = Offset(c.x - starR, c.y),
            end = Offset(c.x + starR, c.y),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
        // Diagonal accents for 4-point star shape
        drawLine(
            color = AccentHotPink.copy(alpha = 0.6f),
            start = Offset(c.x - starInner, c.y - starInner),
            end = Offset(c.x + starInner, c.y + starInner),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = AccentHotPink.copy(alpha = 0.6f),
            start = Offset(c.x + starInner, c.y - starInner),
            end = Offset(c.x - starInner, c.y + starInner),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // Small sparkle dots
        drawCircle(AccentHotPink, 2.5f, Offset(c.x + radius * 0.7f, c.y - radius * 0.7f))
        drawCircle(AccentHotPink.copy(alpha = 0.6f), 2f, Offset(c.x - radius * 0.8f, c.y - radius * 0.5f))
        drawCircle(AccentHotPink.copy(alpha = 0.4f), 1.5f, Offset(c.x + radius * 0.5f, c.y + radius * 0.8f))
    }
}

@Composable
private fun NovelIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val padX = w * 0.15f
        val padY = h * 0.15f
        val contentW = w * 0.70f
        val contentH = h * 0.70f
        val cornerR = minOf(contentW, contentH) * 0.25f

        // Open book shape (HoneyCream fill)
        drawRoundRect(
            color = HoneyCream,
            topLeft = Offset(padX, padY),
            size = Size(contentW, contentH),
            cornerRadius = CornerRadius(cornerR)
        )

        // Center spine line
        val spineX = padX + contentW * 0.5f
        drawLine(
            color = PrimaryPink.copy(alpha = 0.7f),
            start = Offset(spineX, padY + contentH * 0.10f),
            end = Offset(spineX, padY + contentH * 0.90f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )

        // 3 text lines on left page (PrimaryPink)
        repeat(3) { index ->
            val lineY = padY + contentH * (0.28f + index * 0.22f)
            drawLine(
                color = PrimaryPink.copy(alpha = 0.6f),
                start = Offset(padX + contentW * 0.10f, lineY),
                end = Offset(padX + contentW * 0.40f, lineY),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }

        // 3 text lines on right page (PrimaryPink)
        repeat(3) { index ->
            val lineY = padY + contentH * (0.28f + index * 0.22f)
            drawLine(
                color = PrimaryPink.copy(alpha = 0.6f),
                start = Offset(padX + contentW * 0.60f, lineY),
                end = Offset(padX + contentW * 0.90f, lineY),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun ToolboxCard(item: ToolboxItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = PrimaryPink.copy(alpha = 0.12f),
                spotColor = PrimaryPink.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(item.colors))
            .kawaiiClickable(pressedScale = 0.96f, onClick = item.onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon on the LEFT side (40% width)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                item.icon()
            }
            // Text on the RIGHT side (60%)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    item.title,
                    color = AccentHotPink,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    item.subtitle,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
