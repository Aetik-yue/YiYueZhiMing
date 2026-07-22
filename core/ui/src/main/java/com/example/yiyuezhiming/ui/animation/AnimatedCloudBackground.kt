package com.example.yiyuezhiming.ui.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun AnimatedCloudBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = MaterialTheme.colorScheme.background == DarkBackground
    val transition = rememberInfiniteTransition(label = "clouds")
    val drift by transition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Restart),
        label = "cloud-drift"
    )
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                Brush.verticalGradient(
                    if (dark) listOf(DarkBackground, DarkSurface) else listOf(SoftBlush, BackgroundPink, PrimaryPink.copy(alpha = 0.55f))
                )
            )
            val cloudColor = if (dark) Color(0x66918BA7) else CloudWhite.copy(alpha = 0.78f)
            drawCloud(Offset(size.width * (0.18f + drift * 0.08f), size.height * 0.13f), 70f, cloudColor)
            drawCloud(Offset(size.width * (0.82f - drift * 0.1f), size.height * 0.28f), 92f, cloudColor.copy(alpha = 0.58f))
            drawCloud(Offset(size.width * (0.25f + drift * 0.06f), size.height * 0.78f), 110f, cloudColor.copy(alpha = 0.5f))
            repeat(6) { i ->
                val x = (i * 137f + drift * 120f) % size.width
                val y = (i * 173f + 40f) % size.height
                rotate(i * 23f + drift * 40f, Offset(x, y)) {
                    drawHeart(Offset(x, y), 7f + i % 3, PrimaryPink.copy(alpha = if (dark) 0.14f else 0.20f))
                }
            }
            repeat(4) { i ->
                drawPaw(
                    Offset((i * 157f + drift * 60f) % size.width, (i * 199f + 80f) % size.height),
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
