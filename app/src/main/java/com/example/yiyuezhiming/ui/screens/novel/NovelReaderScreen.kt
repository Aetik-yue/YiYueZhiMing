package com.example.yiyuezhiming.ui.screens.novel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.yiyuezhiming.model.ReaderSettings
import com.example.yiyuezhiming.model.ReaderTheme
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.theme.AccentHotPink

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
    val chapter = state.currentChapter
    val pages = chapter?.pages.orEmpty()
    val pagerState = rememberPagerState(
        initialPage = state.pageIndex.coerceAtLeast(0),
        pageCount = { pages.size.coerceAtLeast(1) }
    )

    LaunchedEffect(state.chapterIndex) {
        pagerState.scrollToPage(state.pageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0)))
    }
    LaunchedEffect(pagerState.currentPage) {
        if (pages.isNotEmpty() && pagerState.currentPage != state.pageIndex) {
            viewModel.goTo(state.chapterIndex, pagerState.currentPage)
        }
    }

    Box(Modifier.fillMaxSize().background(colors.first)) {
        when {
            state.isLoading -> Text("正在打开...", Modifier.align(Alignment.Center), color = colors.second)
            state.error != null -> Text(state.error.orEmpty(), Modifier.align(Alignment.Center), color = colors.second)
            chapter == null -> Text("还没有可阅读章节", Modifier.align(Alignment.Center), color = colors.second)
            else -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { viewModel.toggleMenu() },
                    contentPadding = PaddingValues(horizontal = state.settings.pagePadding.dp)
                ) { page ->
                    val current = pages.getOrNull(page)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 34.dp),
                        verticalArrangement = Arrangement.spacedBy(state.settings.lineSpacing.dp)
                    ) {
                        Text(chapter.title, color = colors.second.copy(alpha = 0.55f), style = MaterialTheme.typography.bodySmall)
                        current?.lines.orEmpty().forEach { line ->
                            Text(
                                line.text,
                                color = colors.second,
                                fontSize = state.settings.fontSizeSp.sp,
                                lineHeight = (state.settings.fontSizeSp + state.settings.lineSpacing).sp
                            )
                        }
                    }
                }
            }
        }

        if (state.menuVisible) {
            ReaderMenu(
                title = state.book?.title.orEmpty(),
                progress = state.progressText,
                onBack = onBack,
                onToc = { showToc = true },
                onSettings = { showSettings = true },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showToc) {
        ModalBottomSheet(onDismissRequest = { showToc = false }) {
            Text("目录", Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = AccentHotPink, fontWeight = FontWeight.ExtraBold)
            LazyColumn(contentPadding = PaddingValues(bottom = 28.dp)) {
                items(state.chapters) { item ->
                    Text(
                        item.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .kawaiiClickable {
                                viewModel.goTo(item.index)
                                showToc = false
                            }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        color = if (item.index == state.chapterIndex) AccentHotPink else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showSettings) {
        ReaderSettingsSheet(
            settings = state.settings,
            onDismiss = { showSettings = false },
            onChange = viewModel::updateSettings
        )
    }
}

@Composable
private fun ReaderMenu(
    title: String,
    progress: String,
    onBack: () -> Unit,
    onToc: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.58f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("返回", color = Color.White) }
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(progress, color = Color.White.copy(alpha = 0.72f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = onToc) { Text("目录", color = Color.White) }
            TextButton(onClick = onSettings) { Text("设置", color = Color.White) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSettingsSheet(
    settings: ReaderSettings,
    onDismiss: () -> Unit,
    onChange: (ReaderSettings) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("阅读设置", color = AccentHotPink, fontWeight = FontWeight.ExtraBold)
            SettingSlider("字号", settings.fontSizeSp, 16f..30f) { onChange(settings.copy(fontSizeSp = it)) }
            SettingSlider("行距", settings.lineSpacing, 4f..18f) { onChange(settings.copy(lineSpacing = it)) }
            SettingSlider("边距", settings.pagePadding, 12f..40f) { onChange(settings.copy(pagePadding = it)) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeChip("白天", settings.theme == ReaderTheme.DAY) { onChange(settings.copy(theme = ReaderTheme.DAY)) }
                ThemeChip("夜间", settings.theme == ReaderTheme.NIGHT) { onChange(settings.copy(theme = ReaderTheme.NIGHT)) }
                ThemeChip("护眼", settings.theme == ReaderTheme.EYE) { onChange(settings.copy(theme = ReaderTheme.EYE)) }
            }
        }
    }
}

@Composable
private fun SettingSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Text("$label ${value.toInt()}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun ThemeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (selected) AccentHotPink else Color.White, RoundedCornerShape(999.dp))
            .kawaiiClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(text, color = if (selected) Color.White else AccentHotPink, fontWeight = FontWeight.Bold)
    }
}

private fun readerColors(theme: ReaderTheme): Pair<Color, Color> = when (theme) {
    ReaderTheme.DAY -> Color(0xFFFFFBF7) to Color(0xFF3D3035)
    ReaderTheme.NIGHT -> Color(0xFF151118) to Color(0xFFEDE3EA)
    ReaderTheme.EYE -> Color(0xFFF5EEDC) to Color(0xFF3C3128)
}
