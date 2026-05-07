package com.example.yiyuezhiming.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.R
import com.example.yiyuezhiming.ui.animation.drawHeart
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.TextBrown

@Composable
fun AppLogoIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.app_logo_heart_arrow),
        contentDescription = "以越之名应用标志",
        modifier = modifier.size(40.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun PixelSamoyedIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.pixel_samoyed),
        contentDescription = "萨摩耶像素头像",
        modifier = modifier.size(34.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun PixelAlaskaIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.pixel_alaska),
        contentDescription = "阿拉斯加像素头像",
        modifier = modifier.size(88.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun KawaiiCalendarIcon(modifier: Modifier = Modifier) {
    Canvas(modifier.size(32.dp).semantics { contentDescription = "日期" }) {
        val c = Offset(size.width / 2f, size.height / 2f)
        drawRoundRect(
            color = CloudWhite,
            topLeft = Offset(4f, 6f),
            size = androidx.compose.ui.geometry.Size(size.width - 8f, size.height - 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f)
        )
        drawRoundRect(
            color = AccentHotPink,
            topLeft = Offset(4f, 6f),
            size = androidx.compose.ui.geometry.Size(size.width - 8f, 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f)
        )
        drawCircle(PrimaryPink, 3f, Offset(11f, 8f))
        drawCircle(PrimaryPink, 3f, Offset(size.width - 11f, 8f))
        drawHeart(c + Offset(0f, 5f), 5f, AccentHotPink)
    }
}

@Composable
fun CatGearIcon(modifier: Modifier = Modifier, contentDescription: String = "设置") {
    Canvas(modifier.size(44.dp).semantics { this.contentDescription = contentDescription }) {
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(CloudWhite, 17f, c)
        drawLine(PrimaryPink, Offset(c.x - 12f, c.y - 22f), Offset(c.x - 3f, c.y - 14f), 6f, StrokeCap.Round)
        drawLine(PrimaryPink, Offset(c.x + 12f, c.y - 22f), Offset(c.x + 3f, c.y - 14f), 6f, StrokeCap.Round)
        repeat(8) { i ->
            val a = Math.toRadians((i * 45).toDouble())
            drawLine(
                AccentHotPink,
                Offset(c.x + kotlin.math.cos(a).toFloat() * 16f, c.y + kotlin.math.sin(a).toFloat() * 16f),
                Offset(c.x + kotlin.math.cos(a).toFloat() * 20f, c.y + kotlin.math.sin(a).toFloat() * 20f),
                3f,
                StrokeCap.Round
            )
        }
        drawCircle(AccentHotPink.copy(alpha = 0.45f), 6f, c, style = Stroke(3f))
        drawCircle(TextBrown, 2f, Offset(c.x - 6f, c.y - 2f))
        drawCircle(TextBrown, 2f, Offset(c.x + 6f, c.y - 2f))
    }
}

@Composable
fun PawIcon(modifier: Modifier = Modifier) {
    Canvas(modifier.size(28.dp)) {
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(AccentHotPink, 5f, c + Offset(0f, 5f))
        drawCircle(PrimaryPink, 3.5f, c + Offset(-8f, -3f))
        drawCircle(PrimaryPink, 3.5f, c + Offset(0f, -6f))
        drawCircle(PrimaryPink, 3.5f, c + Offset(8f, -3f))
    }
}

@Composable
fun MusicCatIcon(modifier: Modifier = Modifier) {
    Box(modifier) {
        Canvas(Modifier.size(34.dp)) {
            val c = Offset(size.width * 0.45f, size.height * 0.54f)
            drawCircle(PrimaryPink, 9f, c)
            drawLine(AccentHotPink, c + Offset(8f, -18f), c + Offset(8f, 0f), 4f, StrokeCap.Round)
            drawLine(AccentHotPink, c + Offset(8f, -18f), c + Offset(20f, -14f), 4f, StrokeCap.Round)
            drawHeart(c + Offset(-10f, -10f), 5f, AccentHotPink)
        }
    }
}

@Composable
fun ToolboxIcon(modifier: Modifier = Modifier) {
    Canvas(modifier.size(34.dp).semantics { contentDescription = "百宝箱" }) {
        val boxTop = size.height * 0.34f
        drawRoundRect(
            color = CloudWhite,
            topLeft = Offset(size.width * 0.16f, boxTop),
            size = androidx.compose.ui.geometry.Size(size.width * 0.68f, size.height * 0.46f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )
        drawRoundRect(
            color = PrimaryPink,
            topLeft = Offset(size.width * 0.12f, size.height * 0.24f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.76f, size.height * 0.2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )
        drawLine(
            color = AccentHotPink,
            start = Offset(size.width * 0.5f, size.height * 0.26f),
            end = Offset(size.width * 0.5f, size.height * 0.82f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        drawHeart(Offset(size.width * 0.5f, size.height * 0.56f), 5.5f, AccentHotPink)
    }
}
