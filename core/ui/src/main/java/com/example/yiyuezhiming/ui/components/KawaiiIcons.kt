package com.example.yiyuezhiming.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.core.ui.R
import com.example.yiyuezhiming.ui.animation.drawHeart
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.SecondaryPink
import com.example.yiyuezhiming.ui.theme.TextBrown
import kotlin.math.cos
import kotlin.math.sin

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
        val w = size.width
        val h = size.height
        // Content area: 70% of canvas, 15% padding each side
        val padX = w * 0.15f
        val padY = h * 0.15f
        val contentW = w * 0.70f
        val contentH = h * 0.70f
        val cornerR = minOf(contentW, contentH) * 0.25f

        // Rounded rectangle body (CloudWhite fill)
        drawRoundRect(
            color = CloudWhite,
            topLeft = Offset(padX, padY),
            size = Size(contentW, contentH),
            cornerRadius = CornerRadius(cornerR)
        )

        // Top strip (PrimaryPink)
        val stripH = contentH * 0.28f
        drawRoundRect(
            color = PrimaryPink,
            topLeft = Offset(padX, padY),
            size = Size(contentW, stripH),
            cornerRadius = CornerRadius(cornerR, cornerR)
        )

        // Two small binding circles at top
        val circleY = padY + stripH * 0.5f
        val circleR = contentW * 0.06f
        drawCircle(TextBrown, circleR, Offset(padX + contentW * 0.3f, circleY))
        drawCircle(TextBrown, circleR, Offset(padX + contentW * 0.7f, circleY))

        // Small heart in center of body
        val heartCenter = Offset(padX + contentW * 0.5f, padY + contentH * 0.62f)
        drawHeart(heartCenter, contentW * 0.14f, AccentHotPink)
    }
}

@Composable
fun CatGearIcon(modifier: Modifier = Modifier, contentDescription: String = "设置") {
    Canvas(modifier.size(44.dp).semantics { this.contentDescription = contentDescription }) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val outerR = size.minDimension * 0.35f
        val toothLen = size.minDimension * 0.10f
        val toothWidth = 5f

        // 6 short rounded teeth around the gear (PrimaryPink)
        repeat(6) { i ->
            val angle = Math.toRadians((i * 60).toDouble())
            val cosA = cos(angle).toFloat()
            val sinA = sin(angle).toFloat()
            drawLine(
                color = PrimaryPink,
                start = Offset(c.x + cosA * outerR, c.y + sinA * outerR),
                end = Offset(c.x + cosA * (outerR + toothLen), c.y + sinA * (outerR + toothLen)),
                strokeWidth = toothWidth,
                cap = StrokeCap.Round
            )
        }

        // Main circle body (CloudWhite fill)
        drawCircle(CloudWhite, outerR, c)

        // Smaller inner circle outline (AccentHotPink)
        drawCircle(
            color = AccentHotPink.copy(alpha = 0.5f),
            radius = outerR * 0.45f,
            center = c,
            style = Stroke(width = 3f)
        )

        // Two dot eyes (TextBrown) - subtle cat face
        val eyeOffsetX = outerR * 0.28f
        val eyeY = c.y - outerR * 0.08f
        drawCircle(TextBrown, outerR * 0.10f, Offset(c.x - eyeOffsetX, eyeY))
        drawCircle(TextBrown, outerR * 0.10f, Offset(c.x + eyeOffsetX, eyeY))
    }
}

@Composable
fun PawIcon(modifier: Modifier = Modifier) {
    Canvas(modifier.size(28.dp)) {
        val c = Offset(size.width / 2f, size.height / 2f)

        // Subtle shadow circle behind
        drawCircle(PrimaryPink.copy(alpha = 0.15f), size.minDimension * 0.42f, c)

        // Main pad (PrimaryPink)
        drawCircle(PrimaryPink, 5.5f, c + Offset(0f, 4f))

        // Toe beans (SecondaryPink)
        drawCircle(SecondaryPink, 3.5f, c + Offset(-8f, -3f))
        drawCircle(SecondaryPink, 3.5f, c + Offset(0f, -6f))
        drawCircle(SecondaryPink, 3.5f, c + Offset(8f, -3f))
    }
}

@Composable
fun MusicCatIcon(modifier: Modifier = Modifier) {
    Box(modifier) {
        Canvas(Modifier.size(34.dp)) {
            val w = size.width
            val h = size.height
            val padX = w * 0.15f
            val padY = h * 0.15f
            val contentW = w * 0.70f
            val contentH = h * 0.70f

            // Music note: circle head (PrimaryPink)
            val noteHeadCenter = Offset(padX + contentW * 0.35f, padY + contentH * 0.75f)
            val noteHeadR = contentW * 0.18f
            drawCircle(PrimaryPink, noteHeadR, noteHeadCenter)

            // Stem (PrimaryPink, rounded cap)
            val stemStart = Offset(noteHeadCenter.x + noteHeadR * 0.8f, noteHeadCenter.y)
            val stemEnd = Offset(noteHeadCenter.x + noteHeadR * 0.8f, padY + contentH * 0.15f)
            drawLine(PrimaryPink, stemStart, stemEnd, 5f, StrokeCap.Round)

            // Flag (PrimaryPink, rounded cap)
            val flagEnd = Offset(stemEnd.x + contentW * 0.25f, stemEnd.y + contentH * 0.15f)
            drawLine(PrimaryPink, stemEnd, flagEnd, 5f, StrokeCap.Round)

            // Small heart accent (AccentHotPink)
            drawHeart(
                Offset(padX + contentW * 0.72f, padY + contentH * 0.65f),
                contentW * 0.12f,
                AccentHotPink
            )
        }
    }
}

@Composable
fun ToolboxIcon(modifier: Modifier = Modifier) {
    Canvas(modifier.size(34.dp).semantics { contentDescription = "百宝箱" }) {
        val w = size.width
        val h = size.height
        val padX = w * 0.15f
        val padY = h * 0.15f
        val contentW = w * 0.70f
        val contentH = h * 0.70f
        val cornerR = minOf(contentW, contentH) * 0.25f

        // Rounded rectangle bottom / box body (CloudWhite)
        val bodyTop = padY + contentH * 0.30f
        val bodyH = contentH * 0.70f
        drawRoundRect(
            color = CloudWhite,
            topLeft = Offset(padX, bodyTop),
            size = Size(contentW, bodyH),
            cornerRadius = CornerRadius(cornerR)
        )

        // Lid line (PrimaryPink) - a rounded strip on top
        val lidH = contentH * 0.22f
        drawRoundRect(
            color = PrimaryPink,
            topLeft = Offset(padX, padY + contentH * 0.12f),
            size = Size(contentW, lidH),
            cornerRadius = CornerRadius(cornerR * 0.8f)
        )

        // Small heart clasp in center (AccentHotPink)
        val claspCenter = Offset(padX + contentW * 0.5f, bodyTop + bodyH * 0.45f)
        drawHeart(claspCenter, contentW * 0.13f, AccentHotPink)
    }
}
