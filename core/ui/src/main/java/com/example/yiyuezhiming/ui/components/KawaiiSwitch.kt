package com.example.yiyuezhiming.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.PrimaryPink

@Composable
fun KawaiiSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbX by animateDpAsState(if (checked) 34.dp else 4.dp, label = "kawaii-switch")
    Box(
        modifier
            .size(width = 72.dp, height = 38.dp)
            .background(if (checked) AccentHotPink else PrimaryPink, RoundedCornerShape(50))
            .kawaiiClickable { onCheckedChange(!checked) }
    ) {
        Canvas(Modifier.size(34.dp).offset(x = thumbX, y = 2.dp)) {
            drawCircle(Color.White, 16f, Offset(size.width / 2f, size.height / 2f))
            drawOval(Color.White, topLeft = Offset(8f, 0f), size = androidx.compose.ui.geometry.Size(7f, 14f))
            drawOval(Color.White, topLeft = Offset(19f, 0f), size = androidx.compose.ui.geometry.Size(7f, 14f))
            drawCircle(PrimaryPink.copy(alpha = 0.55f), 3f, Offset(12f, 20f))
            drawCircle(PrimaryPink.copy(alpha = 0.55f), 3f, Offset(22f, 20f))
        }
    }
}
