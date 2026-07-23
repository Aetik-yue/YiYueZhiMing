package com.example.yiyuezhiming.ui.animation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.yiyuezhiming.ui.theme.BackgroundPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.DarkBackground
import com.example.yiyuezhiming.ui.theme.DarkSurface
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.SoftBlush

/**
 * 柔和云朵背景。
 *
 * 性能说明：背景为静态绘制（渐变 + 云朵 + 少量爱心/爪印），Canvas 只在尺寸或主题变化时
 * 重绘一次，不会每帧刷新，避免在所有页面持续占用 GPU 导致交互卡顿。
 */
@Composable
fun AnimatedCloudBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = MaterialTheme.colorScheme.background == DarkBackground
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                Brush.verticalGradient(
                    if (dark) listOf(DarkBackground, DarkSurface) else listOf(SoftBlush, BackgroundPink, PrimaryPink.copy(alpha = 0.55f))
                )
            )
            val cloudColor = if (dark) Color(0x66918BA7) else CloudWhite.copy(alpha = 0.78f)
            drawCloud(Offset(size.width * 0.20f, size.height * 0.13f), 70f, cloudColor)
            drawCloud(Offset(size.width * 0.78f, size.height * 0.26f), 92f, cloudColor.copy(alpha = 0.58f))
            drawCloud(Offset(size.width * 0.28f, size.height * 0.80f), 110f, cloudColor.copy(alpha = 0.5f))
            repeat(6) { i ->
                val x = (i * 137f + 40f) % size.width
                val y = (i * 173f + 60f) % size.height
                rotate(i * 23f, Offset(x, y)) {
                    drawHeart(Offset(x, y), 7f + i % 3, PrimaryPink.copy(alpha = if (dark) 0.14f else 0.20f))
                }
            }
            repeat(4) { i ->
                drawPaw(
                    Offset((i * 157f + 50f) % size.width, (i * 199f + 90f) % size.height),
                    PrimaryPink.copy(alpha = if (dark) 0.08f else 0.14f)
                )
            }
        }
        content()
    }
}

fun DrawScope.drawCloud(center: Offset, radius: Float, color: Color) {
    drawCircle(color, radius * 0.46f, Offset(center.x - radius * 0.45f, center.y + radius * 0.08f))
    drawCircle(color, radius * 0.56f, Offset(center.x, center.y - radius * 0.12f))
    drawCircle(color, radius * 0.42f, Offset(center.x + radius * 0.47f, center.y + radius * 0.1f))
    drawRoundRect(
        color,
        topLeft = Offset(center.x - radius * 0.9f, center.y),
        size = androidx.compose.ui.geometry.Size(radius * 1.8f, radius * 0.55f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius)
    )
}

fun DrawScope.drawPaw(center: Offset, color: Color) {
    drawCircle(color, 7f, Offset(center.x, center.y + 7f))
    drawCircle(color, 4f, Offset(center.x - 8f, center.y - 2f))
    drawCircle(color, 4f, Offset(center.x, center.y - 5f))
    drawCircle(color, 4f, Offset(center.x + 8f, center.y - 2f))
}
