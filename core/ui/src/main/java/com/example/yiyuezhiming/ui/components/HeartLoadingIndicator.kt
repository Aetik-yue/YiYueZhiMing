package com.example.yiyuezhiming.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.ui.animation.drawHeart
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.PrimaryPink

@Composable
fun HeartLoadingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "heart-loading")
    val pulse by transition.animateFloat(
        0.7f,
        1.15f,
        infiniteRepeatable(tween(520), RepeatMode.Reverse),
        label = "heart-loading-pulse"
    )
    Canvas(modifier.size(42.dp)) {
        val c = Offset(size.width / 2f, size.height / 2f)
        scale(pulse, c) { drawHeart(c, 14f, AccentHotPink) }
        drawHeart(c + Offset(-15f, 4f), 8f, PrimaryPink.copy(alpha = 0.7f))
        drawHeart(c + Offset(15f, 4f), 8f, PrimaryPink.copy(alpha = 0.7f))
    }
}
