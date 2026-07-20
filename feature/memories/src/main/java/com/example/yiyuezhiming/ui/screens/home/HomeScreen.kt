package com.example.yiyuezhiming.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.FloatingHearts
import com.example.yiyuezhiming.ui.animation.StaggeredItem
import com.example.yiyuezhiming.ui.animation.drawHeart
import com.example.yiyuezhiming.ui.components.BearWithAlbum
import com.example.yiyuezhiming.ui.components.EmptyStateView
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.components.MemoryCard
import com.example.yiyuezhiming.ui.components.PixelAlaskaIcon
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onAddMemory: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var fabHearts by remember { mutableStateOf(false) }
    LaunchedEffect(fabHearts) {
        if (fabHearts) {
            delay(900)
            fabHearts = false
        }
    }
    AnimatedCloudBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                KawaiiTopBar(
                    title = "我们的点滴",
                    showLogo = true
                )
            },
            floatingActionButton = {
                Box {
                    if (fabHearts) FloatingHearts(Modifier.size(120.dp).align(Alignment.Center), 9)
                    FloatingActionButton(
                        onClick = {
                            fabHearts = true
                            onAddMemory()
                        },
                        containerColor = AccentHotPink,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Canvas(Modifier.size(62.dp)) {
                                drawOval(Color.White, topLeft = androidx.compose.ui.geometry.Offset(16f, -4f), size = androidx.compose.ui.geometry.Size(9f, 20f))
                                drawOval(Color.White, topLeft = androidx.compose.ui.geometry.Offset(36f, -4f), size = androidx.compose.ui.geometry.Size(9f, 20f))
                            }
                            Text("+", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { AnimatedVisibility(true, enter = fadeIn() + slideInVertically { -24 }) { HeroCard() } }
                when {
                    state.isLoading -> item { Text("正在整理我们的回忆…", color = AccentHotPink) }
                    state.error != null -> item { Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error) }
                    state.memories.isEmpty() -> item {
                        EmptyStateView(
                            title = "这里还没有回忆喔",
                            message = "点一下右下角，把第一份心动偷偷收藏起来吧",
                            buttonText = "添加第一条记忆",
                            onButtonClick = onAddMemory,
                            animal = { BearWithAlbum() },
                            modifier = Modifier.fillMaxWidth().height(420.dp)
                        )
                    }
                    else -> itemsIndexed(state.memories, key = { _, item -> item.id }) { index, memory ->
                        StaggeredItem(index) { MemoryCard(memory, onClick = {}, modifier = Modifier.fillMaxWidth()) }
                    }
                }
                item { Spacer(Modifier.height(82.dp)) }
            }
        }
    }
}

@Composable
private fun HeroCard() {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = PrimaryPink.copy(alpha = 0.18f), spotColor = AccentHotPink.copy(alpha = 0.13f))
            .background(Brush.horizontalGradient(listOf(Color.White, CloudWhite, PrimaryPink.copy(alpha = 0.24f))), RoundedCornerShape(28.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text("我们的回忆", style = MaterialTheme.typography.headlineMedium, color = AccentHotPink, fontWeight = FontWeight.ExtraBold)
            Text("今天也想把你写进日记里", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
        }
        Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
            PixelAlaskaIcon(Modifier.size(96.dp))
            Canvas(Modifier.matchParentSize()) {
                drawHeart(androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.22f), 10f, AccentHotPink.copy(alpha = 0.8f))
            }
        }
    }
}
