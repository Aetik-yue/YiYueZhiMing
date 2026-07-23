package com.example.yiyuezhiming.ui.animation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.example.yiyuezhiming.ui.theme.PrimaryPink

@Composable
fun Modifier.kawaiiClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.96f,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 520f),
        label = "kawaii-click"
    )
    val ripple = ripple(color = PrimaryPink.copy(alpha = 0.35f))
    return scale(scale).let {
        if (onLongClick != null) {
            @OptIn(ExperimentalFoundationApi::class)
            it.combinedClickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple,
                onClick = onClick,
                onLongClick = onLongClick
            )
        } else {
            it.clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple,
                onClick = onClick
            )
        }
    }
}
