package com.example.yiyuezhiming.ui.screens.novel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.yiyuezhiming.data.local.ChapterMeta
import com.example.yiyuezhiming.model.ReaderSettings
import com.example.yiyuezhiming.model.ReaderTheme
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelReaderScreen(
    onBack: () -> Unit,
    viewModel: NovelReaderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showToc by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val colors = readerColors(state.settings.theme)
    val pages = state.pages
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size.coerceAtLeast(1) }
    )

    LaunchedEffect(state.chapterIndex, pages.size) {
        val target = state.pageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (pages.isNotEmpty() && page != state.pageIndex) {
                    viewModel.goToPage(page)
                }
            }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.first)
    ) {
        when {
            state.isLoading -> LoadingContent(
                bookTitle = state.book?.title.orEmpty(),
                textColor = colors.second,
                modifier = Modifier.align(Alignment.Center)
            )

            state.error != null -> Text(
                state.error.orEmpty(),
                Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                color = colors.second,
                textAlign = TextAlign.Center
            )

            pages.isEmpty() -> Text(
                "还没有可阅读章节",
                Modifier.align(Alignment.Center),
                color = colors.second.copy(alpha = 0.6f)
            )

            else -> {
                // Top progress bar
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.TopCenter)
                        .background(colors.second.copy(alpha = 0.08f))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(state.overallProgress.coerceIn(0f, 1f))
                            .height(2.dp)
                            .background(AccentHotPink)
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            val width = size.width.toFloat()
                            detectTapGestures { offset ->
                                when {
                                    width <= 0f -> viewModel.toggleMenu()
                                    offset.x < width * 0.33f -> viewModel.previousPage()
                                    offset.x > width * 0.66f -> viewModel.nextPage()
                                    else -> viewModel.toggleMenu()
                                }
                            }
                        },
                    contentPadding = PaddingValues(horizontal = state.settings.pagePadding.dp)
                ) { page ->
                    val current = pages.getOrNull(page)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp, bottom = 48.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Chapter title header
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                state.chapterTitle,
                                color = colors.second.copy(alpha = 0.45f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Box(
                                Modifier
                                    .width(40.dp)
                                    .height(1.dp)
                                    .background(colors.second.copy(alpha = 0.15f), RoundedCornerShape(1.dp))
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Page content
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(state.settings.lineSpacing.dp)
                        ) {
                            current?.lines.orEmpty().forEachIndexed { index, line ->
                                val text = if (index > 0 && line.text.isNotBlank()) {
                                    "\u3000\u3000${line.text}"
                                } else {
                                    line.text
                                }
                                Text(
                                    text,
                                    color = colors.second,
                                    fontSize = state.settings.fontSizeSp.sp,
                                    lineHeight = (state.settings.fontSizeSp + state.settings.lineSpacing).sp
                                )
                            }
                        }

                        // Page footer
                        Text(
                            "${page + 1} / ${pages.size}",
                            color = colors.second.copy(alpha = 0.25f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Chapter navigation - next chapter pill
                if (state.pageIndex == pages.size - 1 && state.chapterIndex < state.totalChapters - 1) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                            .background(
                                AccentHotPink.copy(alpha = 0.18f),
                                RoundedCornerShape(999.dp)
                            )
                            .kawaiiClickable(pressedScale = 0.92f) { viewModel.nextPage() }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            "下一章 →",
                            color = AccentHotPink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Chapter navigation - previous chapter pill
                if (state.pageIndex == 0 && state.chapterIndex > 0) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                            .background(
                                AccentHotPink.copy(alpha = 0.18f),
                                RoundedCornerShape(999.dp)
                            )
                            .kawaiiClickable(pressedScale = 0.92f) { viewModel.previousPage() }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            "← 上一章",
                            color = AccentHotPink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Bottom menu bar with animation
        AnimatedVisibility(
            visible = state.menuVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReaderMenu(
                title = state.book?.title.orEmpty(),
                progress = state.progressText,
                onBack = onBack,
                onToc = { showToc = true },
                onSettings = { showSettings = true }
            )
        }
    }

    // TOC sheet
    if (showToc) {
        TocSheet(
            chapters = state.chapterMetas,
            currentChapterIndex = state.chapterIndex,
            onDismiss = { showToc = false },
            onSelect = { index ->
                viewModel.goTo(index)
                showToc = false
            }
        )
    }

    // Settings sheet
    if (showSettings) {
        ReaderSettingsSheet(
            settings = state.settings,
            onDismiss = { showSettings = false },
            onChange = viewModel::updateSettings
        )
    }
}

// --- Loading state with pulsing animation ---

@Composable
private fun LoadingContent(
    bookTitle: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading-pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-alpha"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "正在打开...",
            color = textColor.copy(alpha = alpha),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        if (bookTitle.isNotBlank()) {
            Text(
                bookTitle,
                color = textColor.copy(alpha = 0.5f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
        }
    }
}

// --- Bottom menu bar ---

@Composable
private fun ReaderMenu(
    title: String,
    progress: String,
    onBack: () -> Unit,
    onToc: () -> Unit,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.72f),
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top row: back | title | progress
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .kawaiiClickable(pressedScale = 0.88f, onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("←", color = Color.White, fontSize = 20.sp)
            }
            Text(
                title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                progress,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Bottom row: action buttons
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MenuActionButton(label = "目录", onClick = onToc)
            MenuActionButton(label = "设置", onClick = onSettings)
        }
    }
}

@Composable
private fun MenuActionButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .kawaiiClickable(pressedScale = 0.9f, onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// --- TOC (Table of Contents) sheet ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TocSheet(
    chapters: List<ChapterMeta>,
    currentChapterIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    // Auto-scroll to current chapter
    LaunchedEffect(currentChapterIndex) {
        if (chapters.isNotEmpty()) {
            val targetIndex = chapters.indexOfFirst { it.chapterIndex == currentChapterIndex }
                .coerceAtLeast(0)
            listState.animateScrollToItem(targetIndex)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 28.dp)) {
            // Header
            Text(
                "目录 · ${chapters.size}章",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                color = AccentHotPink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(chapters, key = { it.id }) { meta ->
                    val isCurrent = meta.chapterIndex == currentChapterIndex
                    TocItem(
                        meta = meta,
                        isCurrent = isCurrent,
                        onClick = { onSelect(meta.chapterIndex) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TocItem(
    meta: ChapterMeta,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(
                if (isCurrent) PrimaryPink.copy(alpha = 0.15f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .kawaiiClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "${meta.chapterIndex + 1}",
            color = if (isCurrent) AccentHotPink else Color.Gray.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.End
        )
        Text(
            meta.title,
            color = if (isCurrent) AccentHotPink else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

// --- Settings sheet ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSettingsSheet(
    settings: ReaderSettings,
    onDismiss: () -> Unit,
    onChange: (ReaderSettings) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Text(
                "阅读设置",
                color = AccentHotPink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )

            // Font size slider
            SettingSlider(
                label = "字号",
                value = settings.fontSizeSp,
                range = 16f..30f,
                startLabel = "A-",
                endLabel = "A+",
                onChange = { onChange(settings.copy(fontSizeSp = it)) }
            )

            // Line spacing slider
            SettingSlider(
                label = "行距",
                value = settings.lineSpacing,
                range = 4f..18f,
                startLabel = "紧凑",
                endLabel = "宽松",
                onChange = { onChange(settings.copy(lineSpacing = it)) }
            )

            // Page margin slider
            SettingSlider(
                label = "边距",
                value = settings.pagePadding,
                range = 12f..40f,
                startLabel = "窄",
                endLabel = "宽",
                onChange = { onChange(settings.copy(pagePadding = it)) }
            )

            // Theme chips
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "主题",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ThemeChip(
                        text = "白天",
                        previewColor = Color(0xFFFFFBF7),
                        selected = settings.theme == ReaderTheme.DAY
                    ) { onChange(settings.copy(theme = ReaderTheme.DAY)) }
                    ThemeChip(
                        text = "夜间",
                        previewColor = Color(0xFF151118),
                        selected = settings.theme == ReaderTheme.NIGHT
                    ) { onChange(settings.copy(theme = ReaderTheme.NIGHT)) }
                    ThemeChip(
                        text = "护眼",
                        previewColor = Color(0xFFF5EEDC),
                        selected = settings.theme == ReaderTheme.EYE
                    ) { onChange(settings.copy(theme = ReaderTheme.EYE)) }
                }
            }
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    startLabel: String,
    endLabel: String,
    onChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${value.toInt()}",
                color = AccentHotPink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                startLabel,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                fontSize = 12.sp
            )
            Slider(
                value = value,
                onValueChange = onChange,
                valueRange = range,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = AccentHotPink,
                    activeTrackColor = AccentHotPink,
                    inactiveTrackColor = PrimaryPink.copy(alpha = 0.25f)
                )
            )
            Text(
                endLabel,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ThemeChip(
    text: String,
    previewColor: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                if (selected) AccentHotPink else CloudWhite,
                RoundedCornerShape(999.dp)
            )
            .kawaiiClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier
                .size(14.dp)
                .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                .background(previewColor, CircleShape)
        )
        Text(
            text,
            color = if (selected) Color.White else AccentHotPink,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

// --- Theme color helper ---

private fun readerColors(theme: ReaderTheme): Pair<Color, Color> = when (theme) {
    ReaderTheme.DAY -> Color(0xFFFFFBF7) to Color(0xFF3D3035)
    ReaderTheme.NIGHT -> Color(0xFF151118) to Color(0xFFEDE3EA)
    ReaderTheme.EYE -> Color(0xFFF5EEDC) to Color(0xFF3C3128)
}
