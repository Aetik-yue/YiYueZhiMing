package com.example.yiyuezhiming.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.components.AppLogoIcon
import com.example.yiyuezhiming.ui.components.BearWithAlbum
import com.example.yiyuezhiming.ui.components.KawaiiSwitch
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.components.RabbitLogo
import com.example.yiyuezhiming.ui.components.SunMoonBunnies
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.LavenderMist
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

@Composable
fun SettingsScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("love_counter", android.content.Context.MODE_PRIVATE) }
    val today = remember { LocalDate.now() }
    var loveStartDateText by rememberSaveable {
        mutableStateOf(prefs.getString("start_date", null) ?: today.toString())
    }
    var showPrivacyDialog by rememberSaveable { mutableStateOf(false) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    var showLoveDateDialog by rememberSaveable { mutableStateOf(false) }
    var passwordEnabled by rememberSaveable { mutableStateOf(false) }
    var pin by rememberSaveable { mutableStateOf("") }
    val loveStartDate = runCatching { LocalDate.parse(loveStartDateText) }.getOrDefault(today)
    val loveDays = (ChronoUnit.DAYS.between(loveStartDate, today) + 1).coerceAtLeast(1)

    AnimatedCloudBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KawaiiTopBar("设置", showLogo = false) }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LoveDaysCounterCard(
                    days = loveDays,
                    onClick = { showLoveDateDialog = true }
                )

                SettingsSectionTitle("外观设置")
                SettingsCard(
                    title = "夜间模式",
                    subtitle = "把小天地调成柔软夜色",
                    leading = { SunMoonBunnies(Modifier.size(58.dp)) },
                    trailing = { KawaiiSwitch(darkMode, onDarkModeChange) },
                    onClick = { onDarkModeChange(!darkMode) }
                )

                SettingsSectionTitle("隐私与安全")
                SettingsCard(
                    title = "私密说明",
                    text = "所有记录仅保存在本地，只属于你们。",
                    leading = { RabbitLogo(Modifier.size(58.dp)) },
                    onClick = { showPrivacyDialog = true }
                )
                SettingsCard(
                    title = "密码保护",
                    subtitle = if (passwordEnabled) "已开启本地心形锁" else "给小秘密加一把心形锁",
                    leading = { BearWithAlbum() },
                    onClick = { showPasswordDialog = true }
                )
                SettingsCard(
                    title = "安心守护",
                    text = "照片和音乐会复制到应用私有目录；卸载应用时，这些私有副本会随应用一起移除。",
                    leading = { RabbitLogo(Modifier.size(58.dp)) },
                    onClick = { showPrivacyDialog = true }
                )

                SettingsSectionTitle("应用信息")
                SettingsCard(
                    title = "关于我们",
                    subtitle = "以越之名 · 1.0",
                    leading = { AppLogoIcon(Modifier.size(42.dp)) },
                    onClick = { showAboutDialog = true }
                )
                Spacer(Modifier.height(72.dp))
            }
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("本地私密空间") },
            text = { Text("日记、提醒、导入的照片副本和音乐副本都保存在本机。应用不会上传数据，也没有后端账号。") },
            confirmButton = { TextButton(onClick = { showPrivacyDialog = false }) { Text("知道啦") } }
        )
    }

    if (showLoveDateDialog) {
        LoveStartDateDialog(
            value = loveStartDateText,
            today = today,
            onDismiss = { showLoveDateDialog = false },
            onSave = { value ->
                loveStartDateText = value
                prefs.edit().putString("start_date", value).apply()
                showLoveDateDialog = false
            }
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("密码保护") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KawaiiSwitch(checked = passwordEnabled, onCheckedChange = { passwordEnabled = it })
                        Text(if (passwordEnabled) "已开启" else "未开启")
                    }
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it.filter(Char::isDigit).take(6) },
                        label = { Text("设置 4-6 位数字密码") },
                        singleLine = true,
                        enabled = passwordEnabled
                    )
                    Text("当前版本先保存本页开关状态；后续可接入启动锁屏。", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                }
            },
            confirmButton = { TextButton(onClick = { showPasswordDialog = false }) { Text("保存") } },
            dismissButton = { TextButton(onClick = { showPasswordDialog = false }) { Text("取消") } }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("以越之名") },
            text = { Text("版本 1.0\n一款为两个人保留温柔瞬间的本地情侣日记。") },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("喜欢") } }
        )
    }
}

@Composable
private fun LoveDaysCounterCard(
    days: Long,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .kawaiiClickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            PrimaryPink.copy(alpha = 0.58f),
                            LavenderMist.copy(alpha = 0.86f),
                            CloudWhite
                        )
                    )
                )
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "我们已经相爱",
                    color = AccentHotPink,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "$days",
                        color = AccentHotPink,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "天",
                        color = AccentHotPink,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
                Text(
                    "点一下可以修改开始日期",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            SunMoonBunnies(Modifier.size(94.dp))
        }
    }
}

@Composable
private fun LoveStartDateDialog(
    value: String,
    today: LocalDate,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var input by rememberSaveable(value) { mutableStateOf(value) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("恋爱开始日") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        error = null
                    },
                    label = { Text("日期 yyyy-MM-dd") },
                    singleLine = true
                )
                Text(
                    "卡片表面不会显示这个日期，只会显示相爱天数。",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = try {
                        LocalDate.parse(input.trim())
                    } catch (_: DateTimeParseException) {
                        null
                    }
                    when {
                        parsed == null -> error = "请输入正确日期，例如 2026-05-08"
                        parsed.isAfter(today) -> error = "开始日期不能晚于今天"
                        else -> onSave(parsed.toString())
                    }
                }
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
        color = AccentHotPink.copy(alpha = 0.82f),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
    maxSubtitleLines: Int = 1
) {
    Card(
        modifier = Modifier.fillMaxWidth().kawaiiClickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) { leading() }
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = AccentHotPink)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                    maxLines = maxSubtitleLines,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (trailing != null) {
                trailing()
            } else {
                Text("›", color = AccentHotPink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    text: String,
    leading: @Composable () -> Unit,
    onClick: () -> Unit
) = SettingsCard(
    title = title,
    subtitle = text,
    leading = leading,
    onClick = onClick,
    maxSubtitleLines = 2
)
