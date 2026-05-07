package com.example.yiyuezhiming.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.model.Mood
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink

@Composable
fun MoodSelector(
    moods: List<Mood>,
    selected: Mood,
    onSelected: (Mood) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("今天的心情", color = AccentHotPink, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            moods.forEach { mood ->
                val isSelected = mood == selected
                val scale by animateFloatAsState(if (isSelected) 1.16f else 1f, label = "mood")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.foundation.layout.Box(
                        Modifier
                            .size(54.dp)
                            .scale(scale)
                            .background(
                                if (isSelected) Brush.linearGradient(listOf(PrimaryPink, AccentHotPink)) else Brush.linearGradient(listOf(CloudWhite, Color.White)),
                                CircleShape
                            )
                            .kawaiiClickable(onClick = { onSelected(mood) }),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimalFaceIcon(mood.face, Modifier.size(42.dp))
                    }
                    Text(mood.label.take(2), style = MaterialTheme.typography.bodyMedium, color = if (isSelected) AccentHotPink else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
