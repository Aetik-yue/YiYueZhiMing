package com.example.yiyuezhiming.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.PrimaryPink

@Composable
fun KawaiiTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    right: @Composable (() -> Unit)? = null,
    showLogo: Boolean = true
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(6.dp, ambientColor = PrimaryPink.copy(alpha = 0.14f), spotColor = PrimaryPink.copy(alpha = 0.1f))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (showBack) {
                Box(
                    Modifier.size(44.dp).kawaiiClickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("‹", color = AccentHotPink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            } else if (showLogo) {
                AppLogoIcon(Modifier.size(44.dp))
            }
            if (showBack || showLogo) Spacer(Modifier.width(8.dp))
            Text(
                title,
                color = AccentHotPink,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
        right?.invoke()
    }
}
