package com.example.yiyuezhiming.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.ui.animation.drawHeart
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink

@Composable
fun PolaroidPhotoPicker(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(if (selected) -1.5f else -2.2f, label = "polaroid-rotate")
    Box(
        modifier
            .rotate(rotation)
            .scale(if (selected) 1f else 0.98f)
            .kawaiiClickable(pressedScale = 0.97f, onClick = onClick)
            .background(CloudWhite, RoundedCornerShape(28.dp))
            .padding(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .background(Brush.linearGradient(listOf(PrimaryPink.copy(alpha = 0.32f), Color.White, AccentHotPink.copy(alpha = 0.24f))), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                BearWithAlbum(Modifier)
                if (!selected) {
                    Text("点击上传照片", color = AccentHotPink, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
                }
                if (selected) {
                    Canvas(Modifier.matchParentSize()) {
                        drawHeart(Offset(size.width - 28f, 30f), 14f, AccentHotPink)
                    }
                }
            }
            Text(if (selected) "照片已经贴进回忆啦" else "把这份可爱时刻留住吧", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
            Spacer(Modifier.height(8.dp))
        }
    }
}
