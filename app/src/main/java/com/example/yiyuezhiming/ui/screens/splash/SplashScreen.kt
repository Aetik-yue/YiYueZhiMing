package com.example.yiyuezhiming.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.FloatingHearts
import com.example.yiyuezhiming.ui.components.HeartLoadingIndicator
import com.example.yiyuezhiming.ui.components.HuggingBunnies
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        delay(2100)
        onFinished()
    }
    val transition = rememberInfiniteTransition(label = "splash")
    val scale by transition.animateFloat(1f, 1.1f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "heart-pulse")
    val titleY by transition.animateFloat(-5f, 5f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "title-float")
    val heartColorProgress by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "heart-color")
    val heartColor = lerp(PrimaryPink, AccentHotPink, heartColorProgress)

    AnimatedCloudBackground {
        FloatingHearts(Modifier.fillMaxSize(), 12)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(visible, enter = fadeIn(tween(700))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("♥", color = heartColor, style = MaterialTheme.typography.displayLarge, modifier = Modifier.scale(scale))
                    HuggingBunnies(Modifier.scale(scale * 0.92f))
                    Text(
                        "以越之名",
                        color = AccentHotPink,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.offset(y = titleY.dp)
                    )
                    Text("只属于我们的秘密空间", color = PrimaryPink, style = MaterialTheme.typography.bodyLarge)
                    HeartLoadingIndicator()
                    Text("正在翻开属于我们的回忆…", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                }
            }
        }
    }
}
