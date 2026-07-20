package com.example.yiyuezhiming.ui.screens.music

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.ui.animation.drawHeart
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.DeepRose
import com.example.yiyuezhiming.ui.theme.LavenderMist
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private data class MusicTrack(
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val colors: List<Color>
)

@Composable
fun MusicPlayerScreen() {
    val tracks = remember {
        listOf(
            MusicTrack("以越之名", "Our little universe", 238, listOf(AccentHotPink, PrimaryPink, LavenderMist)),
            MusicTrack("晚风点滴", "Memory Tape", 214, listOf(Color(0xFFA78BFA), Color(0xFF67E8F9), DeepRose)),
            MusicTrack("日期花园", "Soft Reminder", 251, listOf(Color(0xFFFFD6A5), PrimaryPink, CloudWhite))
        )
    }
    var trackIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var isShuffleOn by remember { mutableStateOf(false) }
    var isRepeatOn by remember { mutableStateOf(false) }
    var progress by remember(trackIndex) { mutableFloatStateOf(0f) }
    val track = tracks[trackIndex]
    val backgroundColor by animateColorAsState(track.colors.first().copy(alpha = 0.92f), label = "music-bg")

    LaunchedEffect(isPlaying, trackIndex, isRepeatOn) {
        while (isPlaying) {
            delay(1000)
            val nextProgress = progress + 1f / track.durationSeconds
            if (nextProgress >= 1f) {
                if (isRepeatOn) {
                    progress = 0f
                } else {
                    trackIndex = (trackIndex + 1) % tracks.size
                    progress = 0f
                }
            } else {
                progress = nextProgress
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        backgroundColor,
                        Color(0xFF2A1627),
                        Color(0xFF050507)
                    )
                )
            )
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                color = track.colors.getOrElse(1) { AccentHotPink }.copy(alpha = 0.2f),
                radius = size.width * 0.56f,
                center = Offset(size.width * 0.12f, size.height * 0.18f)
            )
            drawCircle(
                color = track.colors.last().copy(alpha = 0.16f),
                radius = size.width * 0.45f,
                center = Offset(size.width * 0.86f, size.height * 0.62f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            KawaiiTopBar(
                title = "音乐",
                showLogo = false,
                modifier = Modifier.background(Color.Transparent)
            )
            Spacer(Modifier.height(22.dp))
            AlbumArt(
                track = track,
                isPlaying = isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 38.dp)
            )
            Spacer(Modifier.height(28.dp))
            Column(Modifier.padding(horizontal = 28.dp)) {
                Text(
                    text = track.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Text(
                    text = track.artist,
                    color = Color.White.copy(alpha = 0.62f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(28.dp))
                MusicProgress(
                    progress = progress,
                    durationSeconds = track.durationSeconds,
                    onProgressChange = { progress = it }
                )
                Spacer(Modifier.height(26.dp))
                PlaybackControls(
                    isPlaying = isPlaying,
                    isShuffleOn = isShuffleOn,
                    isRepeatOn = isRepeatOn,
                    onShuffle = { isShuffleOn = !isShuffleOn },
                    onPrevious = {
                        trackIndex = if (trackIndex == 0) tracks.lastIndex else trackIndex - 1
                        progress = 0f
                    },
                    onPlayPause = { isPlaying = !isPlaying },
                    onNext = {
                        trackIndex = (trackIndex + 1) % tracks.size
                        progress = 0f
                    },
                    onRepeat = { isRepeatOn = !isRepeatOn }
                )
            }
        }
    }
}

@Composable
private fun AlbumArt(
    track: MusicTrack,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cover-breathe")
    val breathing by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "cover-scale"
    )
    val coverScale by animateFloatAsState(
        targetValue = if (isPlaying) breathing else 1f,
        animationSpec = tween(450),
        label = "cover-state-scale"
    )
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(coverScale)
            .shadow(
                elevation = 30.dp,
                shape = RoundedCornerShape(36.dp),
                ambientColor = track.colors.first().copy(alpha = 0.32f),
                spotColor = Color.Black.copy(alpha = 0.32f)
            )
            .clip(RoundedCornerShape(36.dp))
            .background(Brush.linearGradient(track.colors)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize().padding(28.dp)) {
            drawCircle(Color.White.copy(alpha = 0.16f), size.minDimension * 0.36f, center)
            drawCircle(Color.White.copy(alpha = 0.22f), size.minDimension * 0.2f, center)
            drawCircle(Color(0xFF050507).copy(alpha = 0.36f), size.minDimension * 0.08f, center)
            drawHeart(Offset(size.width * 0.28f, size.height * 0.3f), 18f, Color.White.copy(alpha = 0.84f))
            drawHeart(Offset(size.width * 0.7f, size.height * 0.68f), 12f, Color.White.copy(alpha = 0.56f))
        }
        Text(
            text = track.title.take(1),
            color = Color.White,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MusicProgress(
    progress: Float,
    durationSeconds: Int,
    onProgressChange: (Float) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val barHeight by animateDpAsState(if (isDragging) 6.dp else 4.dp, label = "progress-height")
    val thumbSize by animateDpAsState(if (isDragging) 12.dp else 4.dp, label = "thumb-size")
    val thumbAlpha by animateFloatAsState(if (isDragging) 1f else 0.28f, label = "thumb-alpha")
    val currentSeconds = (durationSeconds * progress.coerceIn(0f, 1f)).roundToInt()

    Column {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val sliderWidth = maxWidth
            val widthPx = with(density) { maxWidth.toPx() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .pointerInput(widthPx) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                onProgressChange((offset.x / widthPx).coerceIn(0f, 1f))
                            },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, _ ->
                                change.consume()
                                onProgressChange((change.position.x / widthPx).coerceIn(0f, 1f))
                            }
                        )
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Canvas(Modifier.fillMaxWidth().height(barHeight)) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.18f),
                        size = size,
                        cornerRadius = CornerRadius(999f, 999f)
                    )
                    drawRoundRect(
                        color = Color.White,
                        size = Size(size.width * progress.coerceIn(0f, 1f), size.height),
                        cornerRadius = CornerRadius(999f, 999f)
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(start = ((sliderWidth - thumbSize) * progress.coerceIn(0f, 1f)))
                        .size(thumbSize)
                        .alpha(thumbAlpha)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TimeText(formatSeconds(currentSeconds))
            TimeText(formatSeconds(durationSeconds))
        }
    }
}

@Composable
private fun TimeText(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.45f),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace
    )
}

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    isShuffleOn: Boolean,
    isRepeatOn: Boolean,
    onShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        val railWidth = maxWidth.coerceAtMost(340.dp)
        val nearOffset = railWidth * 0.23f
        val farOffset = railWidth * 0.42f

        Box(
            modifier = Modifier
                .width(railWidth)
                .height(96.dp),
            contentAlignment = Alignment.Center
        ) {
            ToggleControlButton(
                icon = ControlIcon.Shuffle,
                checked = isShuffleOn,
                onClick = onShuffle,
                modifier = Modifier.offset(x = -farOffset).size(44.dp),
                iconSize = 22.dp
            )
            TransportButton(
                icon = ControlIcon.Previous,
                onClick = onPrevious,
                modifier = Modifier.offset(x = -nearOffset).size(52.dp),
                iconSize = 34.dp
            )
            PlayPauseButton(isPlaying, onPlayPause)
            TransportButton(
                icon = ControlIcon.Next,
                onClick = onNext,
                modifier = Modifier.offset(x = nearOffset).size(52.dp),
                iconSize = 34.dp
            )
            ToggleControlButton(
                icon = ControlIcon.Repeat,
                checked = isRepeatOn,
                onClick = onRepeat,
                modifier = Modifier.offset(x = farOffset).size(44.dp),
                iconSize = 22.dp
            )
        }
    }
}

@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val glowAlpha by animateFloatAsState(if (isPlaying) 0.42f else 0.25f, label = "play-glow")
    Box(
        modifier = Modifier
            .size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(AccentHotPink.copy(alpha = glowAlpha), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(
                    elevation = 30.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = AccentHotPink.copy(alpha = 0.25f)
                )
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFF67E8F9))))
                .kawaiiClickable(pressedScale = 0.92f, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = { fadeIn(tween(120)) togetherWith fadeOut(tween(120)) },
                label = "play-pause-icon"
            ) { playing ->
                ControlIconCanvas(
                    icon = if (playing) ControlIcon.Pause else ControlIcon.Play,
                    color = Color(0xFF050507),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun TransportButton(
    icon: ControlIcon,
    onClick: () -> Unit,
    modifier: Modifier,
    iconSize: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = modifier.kawaiiClickable(pressedScale = 0.9f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        ControlIconCanvas(icon, Color.White.copy(alpha = 0.9f), Modifier.size(iconSize))
    }
}

@Composable
private fun ToggleControlButton(
    icon: ControlIcon,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    iconSize: androidx.compose.ui.unit.Dp
) {
    val color by animateColorAsState(if (checked) AccentHotPink else Color.White.copy(alpha = 0.55f), label = "toggle-color")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = modifier.kawaiiClickable(pressedScale = 0.9f, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            ControlIconCanvas(icon, color, Modifier.size(iconSize))
        }
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(color)
                .alpha(if (checked) 1f else 0f)
        )
    }
}

private enum class ControlIcon {
    Shuffle, Previous, Play, Pause, Next, Repeat
}

@Composable
private fun ControlIconCanvas(
    icon: ControlIcon,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        when (icon) {
            ControlIcon.Play -> {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.34f, h * 0.22f)
                    lineTo(w * 0.34f, h * 0.78f)
                    lineTo(w * 0.78f, h * 0.5f)
                    close()
                }
                drawPath(path, color)
            }
            ControlIcon.Pause -> {
                drawRoundRect(color, Offset(w * 0.28f, h * 0.22f), Size(w * 0.16f, h * 0.56f), CornerRadius(5f, 5f))
                drawRoundRect(color, Offset(w * 0.56f, h * 0.22f), Size(w * 0.16f, h * 0.56f), CornerRadius(5f, 5f))
            }
            ControlIcon.Previous -> {
                drawLine(color, Offset(w * 0.2f, h * 0.24f), Offset(w * 0.2f, h * 0.76f), 4f, StrokeCap.Round)
                drawTriangle(color, Offset(w * 0.75f, h * 0.2f), Offset(w * 0.32f, h * 0.5f), Offset(w * 0.75f, h * 0.8f))
            }
            ControlIcon.Next -> {
                drawLine(color, Offset(w * 0.8f, h * 0.24f), Offset(w * 0.8f, h * 0.76f), 4f, StrokeCap.Round)
                drawTriangle(color, Offset(w * 0.25f, h * 0.2f), Offset(w * 0.68f, h * 0.5f), Offset(w * 0.25f, h * 0.8f))
            }
            ControlIcon.Shuffle -> {
                drawLine(color, Offset(w * 0.12f, h * 0.32f), Offset(w * 0.42f, h * 0.32f), 3.2f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.42f, h * 0.32f), Offset(w * 0.72f, h * 0.68f), 3.2f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.12f, h * 0.68f), Offset(w * 0.42f, h * 0.68f), 3.2f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.42f, h * 0.68f), Offset(w * 0.72f, h * 0.32f), 3.2f, StrokeCap.Round)
                drawArrowHead(color, Offset(w * 0.78f, h * 0.68f), true)
                drawArrowHead(color, Offset(w * 0.78f, h * 0.32f), true)
            }
            ControlIcon.Repeat -> {
                drawArc(color, -180f, 270f, false, topLeft = Offset(w * 0.16f, h * 0.18f), size = Size(w * 0.66f, h * 0.44f), style = Stroke(3.4f, cap = StrokeCap.Round))
                drawArc(color, 0f, 270f, false, topLeft = Offset(w * 0.18f, h * 0.38f), size = Size(w * 0.66f, h * 0.44f), style = Stroke(3.4f, cap = StrokeCap.Round))
                drawArrowHead(color, Offset(w * 0.76f, h * 0.18f), true)
                drawArrowHead(color, Offset(w * 0.22f, h * 0.82f), false)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTriangle(
    color: Color,
    p1: Offset,
    p2: Offset,
    p3: Offset
) {
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }
    drawPath(path, color)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrowHead(
    color: Color,
    tip: Offset,
    pointsRight: Boolean
) {
    val direction = if (pointsRight) -1f else 1f
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(tip.x + direction * 8f, tip.y - 6f)
        lineTo(tip.x + direction * 8f, tip.y + 6f)
        close()
    }
    drawPath(path, color)
}

private fun formatSeconds(seconds: Int): String {
    val minutes = seconds / 60
    val remaining = seconds % 60
    return "$minutes:${remaining.toString().padStart(2, '0')}"
}
