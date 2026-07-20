package com.example.yiyuezhiming.ui.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.LavenderMist
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import kotlin.random.Random

@Composable
fun FloatingHearts(
    modifier: Modifier = Modifier,
    count: Int = 10
) {
    val seeds = remember { List(count) { Random.nextFloat() } }
    val transition = rememberInfiniteTransition(label = "floating-hearts")
    val progress by transition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "heart-progress"
    )
    val colors = listOf(PrimaryPink, AccentHotPink, LavenderMist)
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            seeds.forEachIndexed { index, seed ->
                val local = ((progress + seed) % 1f)
                val alpha = if (local < 0.5f) local * 2f else (1f - local) * 2f
                val x = size.width * (0.28f + (seed * 0.44f)) + kotlin.math.sin(local * 8f) * 18f
                val y = size.height * 0.72f - local * (70f + seed * 70f)
                val heartSize = 9f + seed * 10f
                rotate(degrees = -16f + seed * 32f, pivot = Offset(x, y)) {
                    scale(scale = 0.7f + local * 0.55f, pivot = Offset(x, y)) {
                        drawHeart(
                            center = Offset(x, y),
                            size = heartSize,
                            color = colors[index % colors.size].copy(alpha = alpha * 0.75f)
                        )
                    }
                }
            }
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeart(
    center: Offset,
    size: Float,
    color: Color
) {
    val p = Path().apply {
        moveTo(center.x, center.y + size * 0.55f)
        cubicTo(center.x - size * 1.35f, center.y - size * 0.25f, center.x - size * 0.7f, center.y - size * 1.15f, center.x, center.y - size * 0.45f)
        cubicTo(center.x + size * 0.7f, center.y - size * 1.15f, center.x + size * 1.35f, center.y - size * 0.25f, center.x, center.y + size * 0.55f)
        close()
    }
    drawPath(p, color)
}
