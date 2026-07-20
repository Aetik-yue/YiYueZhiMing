package com.example.yiyuezhiming.ui.screens.music

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.yiyuezhiming.model.Song
import com.example.yiyuezhiming.ui.animation.drawHeart
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.DeepRose
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    viewModel: MusicPlayerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showPlaylist by remember { mutableStateOf(false) }
    val song = state.currentSong

    LaunchedEffect(Unit) { viewModel.connectToService() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> viewModel.onPermissionResult(granted) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF3A1F2B), Color(0xFF1A0F16), Color(0xFF0A060A))))
    ) {
        PlayerBackground(song)

        when {
            !state.hasPermission -> PermissionView(
                onRequest = {
                    permissionLauncher.launch(
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            android.Manifest.permission.READ_MEDIA_AUDIO
                        } else {
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                    )
                },
                modifier = Modifier.align(Alignment.Center)
            )

            state.isLoading -> LoadingView(Modifier.align(Alignment.Center))

            state.queue.isEmpty() -> EmptyView(Modifier.align(Alignment.Center))

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    PlayerTopBar(
                        onBack = { },
                        onMore = { showPlaylist = true }
                    )

                    VinylDisc(
                        song = song,
                        isPlaying = state.isPlaying,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp)
                    )

                    SongInfo(song)

                    Spacer(Modifier.height(20.dp))

                    QuickActions(onPlaylist = { showPlaylist = true })

                    Spacer(Modifier.height(24.dp))

                    SeekBar(
                        position = state.position,
                        duration = state.duration,
                        onSeek = viewModel::seekTo
                    )

                    Spacer(Modifier.height(20.dp))

                    PlaybackControls(
                        isPlaying = state.isPlaying,
                        isShuffleOn = state.isShuffleOn,
                        repeatMode = state.repeatMode,
                        onShuffle = viewModel::toggleShuffle,
                        onPrevious = viewModel::previous,
                        onPlayPause = viewModel::togglePlayPause,
                        onNext = viewModel::next,
                        onRepeat = viewModel::cycleRepeatMode
                    )

                    Spacer(Modifier.height(28.dp))
                }
            }
        }
    }

    if (showPlaylist) {
        ModalBottomSheet(onDismissRequest = { showPlaylist = false }) {
            PlaylistSheet(
                queue = state.queue,
                currentIndex = state.currentIndex,
                onSelect = { index ->
                    viewModel.playSong(index)
                    showPlaylist = false
                }
            )
        }
    }
}

@Composable
private fun PlayerBackground(song: Song?) {
    Box(Modifier.fillMaxSize()) {
        song?.albumArtUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(90.dp)
                    .alpha(0.5f),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF2A1620).copy(alpha = 0.55f),
                            Color(0xFF120A10).copy(alpha = 0.75f),
                            Color(0xFF0A060A).copy(alpha = 0.9f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun PlayerTopBar(onBack: () -> Unit, onMore: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .kawaiiClickable(pressedScale = 0.9f, onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(22.dp)) {
                drawLine(Color.White.copy(alpha = 0.85f), Offset(size.width * 0.7f, size.height * 0.15f), Offset(size.width * 0.25f, size.height * 0.5f), 4f, StrokeCap.Round)
                drawLine(Color.White.copy(alpha = 0.85f), Offset(size.width * 0.25f, size.height * 0.5f), Offset(size.width * 0.7f, size.height * 0.85f), 4f, StrokeCap.Round)
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("正在播放", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .kawaiiClickable(pressedScale = 0.9f, onClick = onMore),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(20.dp)) {
                val cy = size.height / 2f
                drawCircle(Color.White.copy(alpha = 0.85f), 3f, Offset(size.width * 0.2f, cy))
                drawCircle(Color.White.copy(alpha = 0.85f), 3f, Offset(size.width * 0.5f, cy))
                drawCircle(Color.White.copy(alpha = 0.85f), 3f, Offset(size.width * 0.8f, cy))
            }
        }
    }
}

@Composable
private fun VinylDisc(song: Song?, isPlaying: Boolean, modifier: Modifier = Modifier) {
    val rotation = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var lastTime = -1L
            while (isActive) {
                withFrameMillis { now ->
                    if (lastTime >= 0) {
                        rotation.floatValue = (rotation.floatValue + (now - lastTime) * 0.018f) % 360f
                    }
                    lastTime = now
                }
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.94f,
        animationSpec = tween(400),
        label = "disc-scale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .aspectRatio(1f)
                .scale(scale)
                .rotate(rotation.floatValue)
                .shadow(
                    elevation = 36.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = AccentHotPink.copy(alpha = 0.2f)
                )
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Color(0xFF181418))
                for (i in 1..7) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = size.minDimension * (0.5f - i * 0.045f),
                        style = Stroke(width = 1.5f)
                    )
                }
                drawCircle(Color(0xFF2A2228), size.minDimension * 0.30f)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                ) {
                    if (song?.albumArtUri != null) {
                        AsyncImage(
                            model = song.albumArtUri,
                            contentDescription = song.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(PrimaryPink, DeepRose))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = song?.title?.take(1) ?: "♪",
                                color = Color.White,
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A060A))
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun SongInfo(song: Song?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = song?.title ?: "未选择歌曲",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = listOfNotNull(song?.artist, song?.album)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
                .ifBlank { "未知歌手" },
            color = Color.White.copy(alpha = 0.55f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuickActions(onPlaylist: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickActionButton { canvasSize ->
            drawHeart(Offset(canvasSize.width / 2f, canvasSize.height / 2f), canvasSize.width * 0.32f, Color.White.copy(alpha = 0.8f))
        }
        QuickActionButton(onClick = onPlaylist) { canvasSize ->
            val w = canvasSize.width
            val h = canvasSize.height
            val c = Color.White.copy(alpha = 0.8f)
            drawLine(c, Offset(w * 0.35f, h * 0.28f), Offset(w * 0.8f, h * 0.28f), 3.5f, StrokeCap.Round)
            drawLine(c, Offset(w * 0.35f, h * 0.5f), Offset(w * 0.8f, h * 0.5f), 3.5f, StrokeCap.Round)
            drawLine(c, Offset(w * 0.35f, h * 0.72f), Offset(w * 0.8f, h * 0.72f), 3.5f, StrokeCap.Round)
            drawCircle(c, 4f, Offset(w * 0.22f, h * 0.28f))
            drawCircle(c, 4f, Offset(w * 0.22f, h * 0.5f))
            drawCircle(c, 4f, Offset(w * 0.22f, h * 0.72f))
        }
        QuickActionButton { canvasSize ->
            val w = canvasSize.width
            val h = canvasSize.height
            val c = Color.White.copy(alpha = 0.8f)
            drawCircle(c, 3f, Offset(w * 0.25f, h * 0.5f))
            drawCircle(c, 3f, Offset(w * 0.5f, h * 0.5f))
            drawCircle(c, 3f, Offset(w * 0.75f, h * 0.5f))
        }
    }
}

@Composable
private fun QuickActionButton(
    onClick: () -> Unit = { },
    icon: androidx.compose.ui.graphics.drawscope.DrawScope.(Size) -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .kawaiiClickable(pressedScale = 0.88f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(26.dp)) { icon(size) }
    }
}

@Composable
private fun SeekBar(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val progress = if (isDragging) dragProgress else (if (duration > 0) position.toFloat() / duration else 0f)
    val barHeight by animateDpAsState(if (isDragging) 5.dp else 3.dp, label = "seek-height")
    val thumbSize by animateDpAsState(if (isDragging) 16.dp else 12.dp, label = "seek-thumb")

    Column(Modifier.padding(horizontal = 28.dp)) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val sliderWidth = maxWidth
            val widthPx = with(density) { maxWidth.toPx() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .pointerInput(widthPx) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                dragProgress = (offset.x / widthPx).coerceIn(0f, 1f)
                            },
                            onDragEnd = {
                                isDragging = false
                                onSeek((dragProgress * duration).toLong())
                            },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, _ ->
                                change.consume()
                                dragProgress = (change.position.x / widthPx).coerceIn(0f, 1f)
                            }
                        )
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Canvas(Modifier.fillMaxWidth().height(barHeight)) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.2f),
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
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TimeText(formatMs(if (isDragging) (dragProgress * duration).toLong() else position))
            TimeText(formatMs(duration))
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
    repeatMode: Int,
    onShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToggleControlButton(
            icon = ControlIcon.Shuffle,
            checked = isShuffleOn,
            onClick = onShuffle,
            modifier = Modifier.size(44.dp),
            iconSize = 22.dp
        )
        TransportButton(
            icon = ControlIcon.Previous,
            onClick = onPrevious,
            modifier = Modifier.size(52.dp),
            iconSize = 32.dp
        )
        PlayPauseButton(isPlaying, onPlayPause)
        TransportButton(
            icon = ControlIcon.Next,
            onClick = onNext,
            modifier = Modifier.size(52.dp),
            iconSize = 32.dp
        )
        Box {
            ToggleControlButton(
                icon = ControlIcon.Repeat,
                checked = repeatMode > 0,
                onClick = onRepeat,
                modifier = Modifier.size(44.dp),
                iconSize = 22.dp
            )
            if (repeatMode == 2) {
                Text(
                    "1",
                    color = AccentHotPink,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayPauseButton(isPlaying: Boolean, onClick: () -> Unit) {
    val glowAlpha by animateFloatAsState(if (isPlaying) 0.4f else 0.22f, label = "play-glow")
    Box(
        modifier = Modifier.size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(AccentHotPink.copy(alpha = glowAlpha), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(
                    elevation = 28.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = AccentHotPink.copy(alpha = 0.3f)
                )
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(PrimaryPink, DeepRose)))
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
                    color = Color.White,
                    modifier = Modifier.size(34.dp)
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
    val color by animateColorAsState(
        if (checked) AccentHotPink else Color.White.copy(alpha = 0.5f),
        label = "toggle-color"
    )
    Box(
        modifier = modifier.kawaiiClickable(pressedScale = 0.9f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        ControlIconCanvas(icon, color, Modifier.size(iconSize))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistSheet(
    queue: List<Song>,
    currentIndex: Int,
    onSelect: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) listState.animateScrollToItem(currentIndex)
    }
    Text(
        "播放列表 · ${queue.size}首",
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        color = AccentHotPink,
        fontWeight = FontWeight.ExtraBold
    )
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        itemsIndexed(queue) { index, item ->
            val isCurrent = index == currentIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .kawaiiClickable { onSelect(index) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        item.title,
                        color = if (isCurrent) AccentHotPink else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        item.artist,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    formatMs(item.duration),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun PermissionView(onRequest: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎵", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text("需要访问你的音乐", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "授权后即可扫描并播放设备里的本地音乐",
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRequest,
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentHotPink)
        ) { Text("授权访问", color = Color.White) }
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "loading")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "loading-alpha"
    )
    Text(
        "正在扫描音乐...",
        modifier = modifier,
        color = Color.White.copy(alpha = alpha),
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun EmptyView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎧", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text("没有找到本地音乐", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("把喜欢的歌下载到手机里，就能在这里播放啦", color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center)
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
                    moveTo(w * 0.36f, h * 0.22f)
                    lineTo(w * 0.36f, h * 0.78f)
                    lineTo(w * 0.8f, h * 0.5f)
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
                drawTriangle(color, Offset(w * 0.78f, h * 0.2f), Offset(w * 0.34f, h * 0.5f), Offset(w * 0.78f, h * 0.8f))
            }
            ControlIcon.Next -> {
                drawLine(color, Offset(w * 0.8f, h * 0.24f), Offset(w * 0.8f, h * 0.76f), 4f, StrokeCap.Round)
                drawTriangle(color, Offset(w * 0.22f, h * 0.2f), Offset(w * 0.66f, h * 0.5f), Offset(w * 0.22f, h * 0.8f))
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

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
