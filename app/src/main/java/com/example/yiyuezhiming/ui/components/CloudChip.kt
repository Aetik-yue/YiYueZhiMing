package com.example.yiyuezhiming.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink

@Composable
fun CloudChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (selected) 1.08f else 1f, label = "chip-scale")
    val textColor by animateColorAsState(if (selected) Color.White else AccentHotPink, label = "chip-text")
    Box(
        modifier
            .scale(scale)
            .kawaiiClickable(onClick = onClick)
            .background(
                brush = if (selected) Brush.horizontalGradient(listOf(PrimaryPink, AccentHotPink)) else Brush.horizontalGradient(listOf(CloudWhite, MaterialTheme.colorScheme.surface)),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(text, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}
