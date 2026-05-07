package com.example.yiyuezhiming.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.model.AnimalFace
import com.example.yiyuezhiming.ui.animation.BreathingAnimal
import com.example.yiyuezhiming.ui.animation.drawHeart
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.TextBrown

@Composable
fun RabbitLogo(modifier: Modifier = Modifier, wink: Boolean = false) {
    Canvas(modifier.size(58.dp)) { drawRabbit(size.minDimension / 2f, wink) }
}

@Composable
fun AnimalFaceIcon(face: AnimalFace, modifier: Modifier = Modifier) {
    Canvas(modifier.size(48.dp)) {
        when (face) {
            AnimalFace.BunnyHappy -> drawRabbit(size.minDimension / 2f, false)
            AnimalFace.CatSmile -> drawCat(size.minDimension / 2f, false)
            AnimalFace.BearCalm -> drawBear(size.minDimension / 2f, false)
            AnimalFace.CatSad -> drawCat(size.minDimension / 2f, true)
            AnimalFace.BunnyPout -> drawRabbit(size.minDimension / 2f, true)
            AnimalFace.BearTear -> drawBear(size.minDimension / 2f, true)
        }
    }
}

@Composable
fun HuggingBunnies(modifier: Modifier = Modifier) {
    BreathingAnimal(modifier) {
        Canvas(Modifier.size(210.dp)) {
            drawHeart(Offset(size.width / 2f, size.height * 0.48f), 78f, PrimaryPink.copy(alpha = 0.28f))
            rotate(-9f, Offset(size.width * 0.42f, size.height * 0.52f)) {
                drawRabbit(72f, false, Offset(size.width * 0.39f, size.height * 0.53f))
            }
            rotate(9f, Offset(size.width * 0.61f, size.height * 0.52f)) {
                drawRabbit(72f, false, Offset(size.width * 0.61f, size.height * 0.53f))
            }
            drawHeart(Offset(size.width / 2f, size.height * 0.73f), 18f, AccentHotPink)
        }
    }
}

@Composable
fun BearWithAlbum(modifier: Modifier = Modifier) {
    BreathingAnimal(modifier) {
        Canvas(Modifier.size(170.dp)) {
            drawBear(92f, false, Offset(size.width / 2f, size.height * 0.48f))
            drawRoundRect(
                PrimaryPink.copy(alpha = 0.75f),
                topLeft = Offset(size.width * 0.22f, size.height * 0.62f),
                size = Size(size.width * 0.56f, size.height * 0.24f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(22f)
            )
            drawHeart(Offset(size.width * 0.5f, size.height * 0.74f), 12f, Color.White)
        }
    }
}

@Composable
fun CatWithAlbum(modifier: Modifier = Modifier) {
    BreathingAnimal(modifier) {
        Canvas(Modifier.size(170.dp)) {
            drawCat(92f, true, Offset(size.width / 2f, size.height * 0.42f))
            drawRoundRect(
                CloudWhite,
                topLeft = Offset(size.width * 0.16f, size.height * 0.6f),
                size = Size(size.width * 0.68f, size.height * 0.26f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f)
            )
            drawLine(TextBrown.copy(alpha = 0.3f), Offset(size.width * 0.28f, size.height * 0.71f), Offset(size.width * 0.72f, size.height * 0.71f), 4f)
        }
    }
}

@Composable
fun SleepingFoxCalendar(modifier: Modifier = Modifier) {
    BreathingAnimal(modifier) {
        Canvas(Modifier.size(190.dp)) {
            drawRoundRect(
                CloudWhite,
                topLeft = Offset(size.width * 0.12f, size.height * 0.25f),
                size = Size(size.width * 0.72f, size.height * 0.55f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(28f)
            )
            drawCircle(PrimaryPink, 8f, Offset(size.width * 0.27f, size.height * 0.32f))
            drawCircle(PrimaryPink, 8f, Offset(size.width * 0.69f, size.height * 0.32f))
            drawHeart(Offset(size.width * 0.48f, size.height * 0.55f), 18f, AccentHotPink.copy(alpha = 0.7f))
            drawFox(Offset(size.width * 0.56f, size.height * 0.55f), 68f)
            drawTextDots(Offset(size.width * 0.78f, size.height * 0.18f))
        }
    }
}

@Composable
fun SunMoonBunnies(modifier: Modifier = Modifier) {
    Canvas(modifier.size(150.dp)) {
        drawCircle(Color(0xFFFFD166), 32f, Offset(size.width * 0.32f, size.height * 0.42f))
        drawRabbit(52f, false, Offset(size.width * 0.32f, size.height * 0.55f))
        drawCircle(Color(0xFFA78BFA), 32f, Offset(size.width * 0.72f, size.height * 0.42f))
        drawRabbit(52f, true, Offset(size.width * 0.72f, size.height * 0.55f))
    }
}

private fun DrawScope.drawRabbit(radius: Float, wink: Boolean, center: Offset = Offset(size.width / 2f, size.height / 2f)) {
    drawOval(CloudWhite, Offset(center.x - radius * 0.46f, center.y - radius * 1.12f), Size(radius * 0.32f, radius * 0.9f))
    drawOval(CloudWhite, Offset(center.x + radius * 0.14f, center.y - radius * 1.12f), Size(radius * 0.32f, radius * 0.9f))
    drawOval(PrimaryPink.copy(alpha = 0.55f), Offset(center.x - radius * 0.39f, center.y - radius * 0.98f), Size(radius * 0.18f, radius * 0.58f))
    drawOval(PrimaryPink.copy(alpha = 0.55f), Offset(center.x + radius * 0.21f, center.y - radius * 0.98f), Size(radius * 0.18f, radius * 0.58f))
    drawCircle(CloudWhite, radius * 0.64f, center)
    drawCircle(PrimaryPink.copy(alpha = 0.45f), radius * 0.12f, Offset(center.x - radius * 0.32f, center.y + radius * 0.08f))
    drawCircle(PrimaryPink.copy(alpha = 0.45f), radius * 0.12f, Offset(center.x + radius * 0.32f, center.y + radius * 0.08f))
    drawEyes(center, radius, wink)
    drawLine(TextBrown, Offset(center.x - 5f, center.y + radius * 0.16f), Offset(center.x + 5f, center.y + radius * 0.16f), 3f, StrokeCap.Round)
}

private fun DrawScope.drawBear(radius: Float, tear: Boolean, center: Offset = Offset(size.width / 2f, size.height / 2f)) {
    val fur = Color(0xFFE8B887)
    drawCircle(fur, radius * 0.24f, Offset(center.x - radius * 0.45f, center.y - radius * 0.42f))
    drawCircle(fur, radius * 0.24f, Offset(center.x + radius * 0.45f, center.y - radius * 0.42f))
    drawCircle(fur, radius * 0.62f, center)
    drawOval(CloudWhite.copy(alpha = 0.82f), Offset(center.x - radius * 0.24f, center.y), Size(radius * 0.48f, radius * 0.34f))
    drawEyes(center, radius, false)
    if (tear) drawCircle(Color(0xFF8EC5FF), radius * 0.06f, Offset(center.x + radius * 0.28f, center.y + radius * 0.12f))
    drawArc(TextBrown, 20f, 140f, false, Offset(center.x - 12f, center.y + radius * 0.08f), Size(24f, 18f), style = Stroke(3f, cap = StrokeCap.Round))
}

private fun DrawScope.drawCat(radius: Float, sad: Boolean, center: Offset = Offset(size.width / 2f, size.height / 2f)) {
    val fur = Color(0xFFFFC37A)
    val leftEar = Path().apply {
        moveTo(center.x - radius * 0.5f, center.y - radius * 0.34f)
        lineTo(center.x - radius * 0.28f, center.y - radius * 0.86f)
        lineTo(center.x - radius * 0.05f, center.y - radius * 0.4f)
        close()
    }
    val rightEar = Path().apply {
        moveTo(center.x + radius * 0.5f, center.y - radius * 0.34f)
        lineTo(center.x + radius * 0.28f, center.y - radius * 0.86f)
        lineTo(center.x + radius * 0.05f, center.y - radius * 0.4f)
        close()
    }
    drawPath(leftEar, fur)
    drawPath(rightEar, fur)
    drawCircle(fur, radius * 0.62f, center)
    drawCircle(PrimaryPink.copy(alpha = 0.35f), radius * 0.1f, Offset(center.x - radius * 0.32f, center.y + radius * 0.1f))
    drawCircle(PrimaryPink.copy(alpha = 0.35f), radius * 0.1f, Offset(center.x + radius * 0.32f, center.y + radius * 0.1f))
    drawEyes(center, radius, sad)
    drawLine(TextBrown.copy(alpha = 0.8f), Offset(center.x - radius * 0.55f, center.y + 3f), Offset(center.x - radius * 0.28f, center.y), 2f)
    drawLine(TextBrown.copy(alpha = 0.8f), Offset(center.x + radius * 0.55f, center.y + 3f), Offset(center.x + radius * 0.28f, center.y), 2f)
}

private fun DrawScope.drawFox(center: Offset, radius: Float) {
    val fur = Color(0xFFFF9F68)
    drawOval(fur, Offset(center.x - radius * 0.72f, center.y - radius * 0.36f), Size(radius * 1.15f, radius * 0.62f))
    drawCircle(fur, radius * 0.42f, Offset(center.x - radius * 0.35f, center.y - radius * 0.32f))
    drawCircle(CloudWhite, radius * 0.18f, Offset(center.x - radius * 0.5f, center.y - radius * 0.18f))
    drawArc(TextBrown, 20f, 140f, false, Offset(center.x - radius * 0.46f, center.y - radius * 0.34f), Size(20f, 12f), style = Stroke(3f))
    drawOval(CloudWhite, Offset(center.x + radius * 0.2f, center.y - radius * 0.5f), Size(radius * 0.72f, radius * 0.38f))
}

private fun DrawScope.drawEyes(center: Offset, radius: Float, wink: Boolean) {
    if (wink) {
        drawArc(TextBrown, 20f, 140f, false, Offset(center.x - radius * 0.34f, center.y - radius * 0.15f), Size(18f, 12f), style = Stroke(3f, cap = StrokeCap.Round))
    } else {
        drawCircle(TextBrown, radius * 0.045f, Offset(center.x - radius * 0.2f, center.y - radius * 0.05f))
    }
    drawArc(TextBrown, 20f, 140f, false, Offset(center.x + radius * 0.12f, center.y - radius * 0.15f), Size(18f, 12f), style = Stroke(3f, cap = StrokeCap.Round))
}

private fun DrawScope.drawTextDots(anchor: Offset) {
    drawCircle(PrimaryPink.copy(alpha = 0.5f), 4f, anchor)
    drawCircle(PrimaryPink.copy(alpha = 0.34f), 3f, anchor + Offset(12f, -12f))
    drawCircle(PrimaryPink.copy(alpha = 0.2f), 2f, anchor + Offset(22f, -25f))
}
