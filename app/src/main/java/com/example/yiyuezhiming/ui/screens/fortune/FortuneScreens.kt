package com.example.yiyuezhiming.ui.screens.fortune

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.yiyuezhiming.data.FortuneRepository
import com.example.yiyuezhiming.model.FortuneRecord
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.LavenderMist
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import java.time.format.DateTimeFormatter

data class FortuneEntry(
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@Composable
fun FortuneHubScreen(
    onOpenSign: () -> Unit,
    onOpenTarot: () -> Unit
) {
    val items = listOf(
        FortuneEntry("签运", "每日一签，温柔解签", onOpenSign),
        FortuneEntry("塔罗牌", "每日单张牌指引", onOpenTarot)
    )
    AnimatedCloudBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KawaiiTopBar("运势", showLogo = true) }
        ) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(items) { item -> FortuneEntryCard(item) }
            }
        }
    }
}

@Composable
private fun FortuneEntryCard(item: FortuneEntry) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(listOf(Color.White, LavenderMist, PrimaryPink.copy(alpha = 0.35f))))
            .kawaiiClickable(pressedScale = 0.95f, onClick = item.onClick)
            .padding(16.dp)
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(AccentHotPink.copy(alpha = 0.12f), size.minDimension * 0.24f, Offset(size.width * 0.78f, size.height * 0.22f))
            drawCircle(CloudWhite.copy(alpha = 0.78f), size.minDimension * 0.16f, Offset(size.width * 0.66f, size.height * 0.34f))
        }
        Column(Modifier.align(Alignment.BottomStart), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.title, color = AccentHotPink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(item.subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun DailySignScreen(viewModel: FortuneViewModel = hiltViewModel()) {
    FortuneDrawScreen(
        type = FortuneRepository.TYPE_SIGN,
        title = "签运",
        buttonText = "抽一签",
        emptyText = "今天还没有抽签，先安静想一个小愿望。",
        viewModel = viewModel
    )
}

@Composable
fun TarotScreen(viewModel: FortuneViewModel = hiltViewModel()) {
    FortuneDrawScreen(
        type = FortuneRepository.TYPE_TAROT,
        title = "塔罗牌",
        buttonText = "抽一张牌",
        emptyText = "今天还没有抽牌，让一张牌给你一点温柔提醒。",
        viewModel = viewModel
    )
}

@Composable
private fun FortuneDrawScreen(
    type: String,
    title: String,
    buttonText: String,
    emptyText: String,
    viewModel: FortuneViewModel
) {
    LaunchedEffect(type) { viewModel.load(type) }
    val state by viewModel.state(type).collectAsState()
    AnimatedCloudBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KawaiiTopBar(title, showLogo = true) }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    FortuneTodayCard(
                        record = state.today,
                        emptyText = emptyText,
                        loading = state.isLoading,
                        onDraw = { viewModel.draw(type) },
                        buttonText = buttonText
                    )
                }
                state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
                item {
                    Text("历史记录", color = AccentHotPink, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                }
                items(state.history, key = { it.id }) { record -> HistoryCard(record) }
            }
        }
    }
}

@Composable
private fun FortuneTodayCard(
    record: FortuneRecord?,
    emptyText: String,
    loading: Boolean,
    onDraw: () -> Unit,
    buttonText: String
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color.White, CloudWhite, PrimaryPink.copy(alpha = 0.28f))))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (record == null) {
                Text(emptyText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
            } else {
                FortuneRecordContent(record)
            }
            Button(
                onClick = onDraw,
                enabled = !loading && record == null,
                colors = ButtonDefaults.buttonColors(containerColor = AccentHotPink)
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.size(8.dp))
                }
                Text(if (record == null) buttonText else "今日已完成")
            }
        }
    }
}

@Composable
private fun HistoryCard(record: FortuneRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(record.date.format(DateTimeFormatter.ofPattern("MM.dd")), color = AccentHotPink, fontWeight = FontWeight.Bold)
            FortuneRecordContent(record, compact = true)
        }
    }
}

@Composable
private fun FortuneRecordContent(record: FortuneRecord, compact: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(record.drawTitle, color = AccentHotPink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text(record.drawSubtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontWeight = FontWeight.SemiBold)
        Text(record.drawContent, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f))
        Text("关键词：${record.keywords}", color = AccentHotPink.copy(alpha = 0.78f), style = MaterialTheme.typography.bodySmall)
        if (!compact) {
            Spacer(Modifier.height(4.dp))
            Text(record.interpretation, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
        } else {
            Text(record.interpretation.take(80), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

