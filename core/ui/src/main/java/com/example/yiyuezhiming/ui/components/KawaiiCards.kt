package com.example.yiyuezhiming.ui.components

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.yiyuezhiming.model.Memory
import com.example.yiyuezhiming.model.Reminder
import com.example.yiyuezhiming.ui.animation.drawHeart
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.SecondaryPink

@Composable
fun MemoryCard(memory: Memory, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = PrimaryPink.copy(alpha = 0.14f), spotColor = AccentHotPink.copy(alpha = 0.12f))
            .kawaiiClickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(memory.dateText, color = PrimaryPink, fontWeight = FontWeight.SemiBold)
                AnimalFaceIcon(memory.mood.face, Modifier.size(38.dp))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BearEarPhotoFrame(
                    color = memory.imageColor,
                    imageUri = memory.photoUri,
                    modifier = Modifier.size(76.dp)
                )
                Column(Modifier.weight(1f)) {
                    Text(memory.note, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(8.dp))
                    val musicText = listOf(memory.artistName, memory.songTitle)
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                    if (musicText.isNotBlank() || memory.musicUri != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MusicCatIcon(Modifier.size(24.dp))
                            Text(
                                if (musicText.isBlank()) "本地音乐" else musicText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AccentHotPink,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (memory.musicUri != null) {
                                Text(
                                    "播放",
                                    color = AccentHotPink,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.kawaiiClickable {
                                        runCatching {
                                            MediaPlayer.create(context, Uri.parse(memory.musicUri)).apply {
                                                setOnCompletionListener { player -> player.release() }
                                                start()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                PawIcon(Modifier.size(22.dp))
            }
        }
    }
}

@Composable
fun BearEarPhotoFrame(
    color: Color,
    modifier: Modifier = Modifier,
    imageUri: String? = null
) {
    Box(modifier) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(PrimaryPink, size.width * 0.14f, Offset(size.width * 0.24f, size.height * 0.1f))
            drawCircle(PrimaryPink, size.width * 0.14f, Offset(size.width * 0.76f, size.height * 0.1f))
            drawRoundRect(CloudWhite, cornerRadius = androidx.compose.ui.geometry.CornerRadius(22f))
        }
        Box(
            Modifier
                .padding(8.dp)
                .matchParentSize()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(color, color.copy(alpha = 0.55f), SecondaryPink.copy(alpha = 0.5f))
                    )
                )
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "照片记忆",
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun AlbumPhotoCard(memory: Memory, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .kawaiiClickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(memory.imageColor, memory.imageColor.copy(alpha = 0.62f), SecondaryPink.copy(alpha = 0.5f))
                    )
                )
        ) {
            if (memory.photoUri != null) {
                SubcomposeAsyncImage(
                    model = memory.photoUri,
                    contentDescription = "相册照片",
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop,
                    error = {
                        Box(
                            Modifier
                                .matchParentSize()
                                .background(CloudWhite.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("照片加载失败", color = AccentHotPink, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            } else {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(CloudWhite.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("照片加载失败", color = AccentHotPink, fontWeight = FontWeight.SemiBold)
                }
            }
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .background(CloudWhite.copy(alpha = 0.82f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    "${memory.mood.label} · ${memory.dateText.takeLast(5)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentHotPink
                )
            }
            Canvas(Modifier.matchParentSize().padding(8.dp)) {
                drawHeart(Offset(size.width - 22f, 24f), 10f, AccentHotPink.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun ReminderEnvelopeCard(reminder: Reminder, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(22.dp), ambientColor = PrimaryPink.copy(alpha = 0.12f), spotColor = PrimaryPink.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .background(Brush.verticalGradient(listOf(PrimaryPink, AccentHotPink)), RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(reminder.dateText, color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
            Column(Modifier.weight(1f)) {
                Text(reminder.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(reminder.type, color = PrimaryPink)
            }
            Box(
                Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text(reminder.daysLeft, color = AccentHotPink, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
