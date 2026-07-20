package com.example.yiyuezhiming.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.BackgroundPink
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import kotlinx.coroutines.delay

@Composable
fun KawaiiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    leadingIcon: (@Composable () -> Unit)? = null,
    error: String? = null
) {
    var focused by remember { androidx.compose.runtime.mutableStateOf(false) }
    var shakeTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(error) {
        if (error != null) {
            repeat(5) {
                shakeTick = if (it % 2 == 0) 1 else -1
                delay(55)
            }
            shakeTick = 0
        }
    }
    val border by animateColorAsState(if (focused) AccentHotPink else PrimaryPink.copy(alpha = 0.7f), label = "field-border")
    val y by animateDpAsState(if (focused) (-2).dp else 0.dp, label = "field-label")
    Column(modifier.offset(x = (shakeTick * 5).dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, Modifier.offset(y = y)) },
            leadingIcon = leadingIcon,
            minLines = minLines,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = border,
                unfocusedBorderColor = border,
                focusedContainerColor = BackgroundPink.copy(alpha = 0.62f),
                unfocusedContainerColor = BackgroundPink.copy(alpha = 0.45f)
            ),
            modifier = modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
        )
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
