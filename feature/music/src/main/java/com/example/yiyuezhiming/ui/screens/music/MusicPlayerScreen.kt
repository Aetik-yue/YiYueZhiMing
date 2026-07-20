package com.example.yiyuezhiming.ui.screens.music

import android.Manifest
import android.os.Build
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.yiyuezhiming.model.Song
import com.example.yiyuezhiming.ui.animation.drawHeart
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import kotlin.math.roundToInt

// Gradient presets for the dark immersive background
private val gradientPresets = listOf(
    listOf(AccentHotPink, Color(0xFF7C3AED), Color(0xFF1E1B4B)),
    listOf(Color(0xFF67E8F9), Color(0xFF3B82F6), Color(0xFF0F172A)),
    listOf(Color(0xFFF472B6), Color(0xFFDB2777), Color(0xFF1C1917)),
    listOf(Color(0xFFA78BFA), Color(0xFF6366F1), Color(0xFF0C0A09))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    viewModel: MusicPlayerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showPlaylist by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    // Request permission on first launch if not yet granted
    LaunchedEffect(Unit) {
        if (!state.hasPermission && !state.isLoading) {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            permissionLauncher.launch(permission)
        }
    }

    // Auto-show playlist when songs loaded but nothing playing
    LaunchedEffect(state.isLoading, state.currentIndex, state.songs.size) {
        if (!state.isLoading && state.songs.isNotEmpty() && state.currentIndex == -1) {
            showPlaylist = true
        }
    }

    when {
        // Permission not granted
        !state.hasPermission && !state.isLoading -> {
            PermissionRequestUI(onRequestPermission = {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                permissionLauncher.launch(permission)
            })
        }

        // Loading
        state.isLoading -> {
            LoadingUI()
        }

        // Empty state
        state.songs.isEmpty() -> {
            EmptyMusicUI()
        }

        // Main player
        else -> {
            val currentSong = state.currentSong
            if (currentSong != null && state.currentIndex >= 0) {
                MainPlayerView(
                    state = state,
                    song = currentSong,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.next() },
                    onPrevious = { viewModel.previous() },
                    onSeekTo = { viewModel.seekTo(it) },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onCycleRepeat = { viewModel.cycleRepeatMode() },
                    onShowPlaylist = { showPlaylist = true }
                )
            } else {
                // Songs loaded but none selected - show prompt + playlist
                SelectSongPromptUI(onShowPlaylist = { showPlaylist = true })
            }

            // Playlist bottom sheet
            if (showPlaylist) {
                PlaylistSheet(
                    songs = state.songs,
                    currentIndex = state.currentIndex,
                    onSongClick = { index ->
                        viewModel.playSong(index)
                        showPlaylist = false
                    },
                    onDismiss = { showPlaylist = false }
                )
            }
        }
    }
}

// region Permission / Loading / Empty states

@Composable
private fun PermissionRequestUI(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2A1627), Color(0xFF050507))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🎵",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "需要访问你的音乐",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "授权后即可播放本地音乐哦~",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(listOf(AccentHotPink, Color(0xFFA78BFA)))
                    )
                    .kawaiiClickable(pressedScale = 0.94f, onClick = onRequestPermission)
                    .padding(horizontal = 32.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "授权访问",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LoadingUI() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading-pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "loading-alpha"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2A1627), Color(0xFF050507))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "正在扫描音乐...",
            color = Color.White.copy(alpha = pulseAlpha),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyMusicUI() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2A1627), Color(0xFF050507))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🎶",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "没有找到本地音乐",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SelectSongPromptUI(onShowPlaylist: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2A1627), Color(0xFF050507))
                )
            )
    ) {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🎧",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "选择一首歌开始播放吧",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .kawaiiClickable(pressedScale = 0.94f, onClick = onShowPlaylist)
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "打开播放列表",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// endregion

// region Main Player

@Composable
private fun MainPlayerView(
    state: MusicPlayerState,
    song: Song,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onShowPlaylist: () -> Unit
) {
    val gradientIndex = state.currentIndex.coerceAtLeast(0) % gradientPresets.size
    val gradientColors = gradientPresets[gradientIndex]
    val backgroundColor by animateColorAsState(
        gradientColors.first().copy(alpha = 0.92f),
        label = "music-bg"
    )

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
        // Ambient circles
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                color = gradientColors.getOrElse(1) { AccentHotPink }.copy(alpha = 0.2f),
                radius = size.width * 0.56f,
                center = Offset(size.width * 0.12f, size.height * 0.18f)
            )
            drawCircle(
                color = gradientColors.last().copy(alpha = 0.16f),
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
                song = song,
                gradientColors = gradientColors,
                isPlaying = state.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 38.dp)
            )
            Spacer(Modifier.height(28.dp))
            Column(Modifier.padding(horizontal = 28.dp)) {
                Text(
                    text = song.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    color = Color.White.copy(alpha = 0.62f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(28.dp))
                MusicProgress(
                    positionMs = state.position,
                    durationMs = state.duration,
                    onSeekTo = onSeekTo
                )
                Spacer(Modifier.height(26.dp))
                PlaybackControls(
                    isPlaying = state.isPlaying,
                    isShuffleOn = state.isShuffleOn,
                    repeatMode = state.repeatMode,
                    onShuffle = onToggleShuffle,
                    onPrevious = onPrevious,
                    onPlayPause = onTogglePlayPause,
                    onNext = onNext,
                    onRepeat = onCycleRepeat
                )
                Spacer(Modifier.height(16.dp))
                // Playlist button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .kawaiiClickable(pressedScale = 0.94f, onClick = onShowPlaylist)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "播放列表",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// endregion

// region Album Art

@Composable
private fun AlbumArt(
    song: Song,
    gradientColors: List<Color>,
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
                ambientColor = gradientColors.first().copy(alpha = 0.32f),
                spotColor = Color.Black.copy(alpha = 0.32f)
            )
            .clip(RoundedCornerShape(36.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (song.albumArtUri != null) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Gradient placeholder with first character
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(gradientColors)),
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
                    text = song.title.take(1),
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// endregion

// region Progress Bar

@Composable
private fun MusicProgress(
    positionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val barHeight by animateDpAsState(if (isDragging) 6.dp else 4.dp, label = "progress-height")
    val thumbSize by animateDpAsState(if (isDragging) 12.dp else 4.dp, label = "thumb-size")
    val thumbAlpha by animateFloatAsState(if (isDragging) 1f else 0.28f, label = "thumb-alpha")

    val progress = if (isDragging) {
        dragProgress
    } else {
        if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    }
    val displayPositionMs = if (isDragging) {
        (dragProgress * durationMs).toLong()
    } else {
        positionMs
    }

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
                                dragProgress = (offset.x / widthPx).coerceIn(0f, 1f)
                            },
                            onDragEnd = {
                                isDragging = false
                                onSeekTo((dragProgress * durationMs).toLong())
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
            TimeText(formatMs(displayPositionMs))
            TimeText(formatMs(durationMs))
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

// endregion

// region Playback Controls

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
            // Repeat with badge for repeat-one mode
            Box(modifier = Modifier.offset(x = farOffset)) {
                ToggleControlButton(
                    icon = ControlIcon.Repeat,
                    checked = repeatMode > 0,
                    onClick = onRepeat,
                    modifier = Modifier.size(44.dp),
                    iconSize = 22.dp
                )
                if (repeatMode == 2) {
                    Text(
                        text = "1",
                        color = AccentHotPink,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-2).dp)
                    )
                }
            }
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
        modifier = Modifier.size(88.dp),
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
    val color by animateColorAsState(
        if (checked) AccentHotPink else Color.White.copy(alpha = 0.55f),
        label = "toggle-color"
    )
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

// endregion

// region Playlist Sheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistSheet(
    songs: List<Song>,
    currentIndex: Int,
    onSongClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    // Auto-scroll to current song
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1A1A2E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text(
                text = "播放列表 · ${songs.size}首",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(songs) { index, song ->
                    val isCurrent = index == currentIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .kawaiiClickable(pressedScale = 0.98f, onClick = { onSongClick(index) })
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                color = if (isCurrent) AccentHotPink else Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                color = if (isCurrent) AccentHotPink.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = formatMs(song.duration),
                            color = Color.White.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// endregion

// region Canvas Icons

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

// endregion

// region Helpers

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

// endregion
