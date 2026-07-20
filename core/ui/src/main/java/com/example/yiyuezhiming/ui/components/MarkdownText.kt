package com.example.yiyuezhiming.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.SoftBlush

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val lines = markdown.replace("\r\n", "\n").split("\n")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        var index = 0
        while (index < lines.size) {
            val line = lines[index]
            val trimmed = line.trim()
            when {
                trimmed.isBlank() -> {
                    index += 1
                }
                trimmed.startsWith("```") -> {
                    val codeLines = mutableListOf<String>()
                    index += 1
                    while (index < lines.size && !lines[index].trim().startsWith("```")) {
                        codeLines += lines[index]
                        index += 1
                    }
                    if (index < lines.size) index += 1
                    CodeBlock(codeLines.joinToString("\n"))
                }
                trimmed.startsWith("### ") -> {
                    MarkdownLine(trimmed.removePrefix("### "), color = AccentHotPink, style = MaterialTheme.typography.titleMedium, weight = FontWeight.ExtraBold)
                    index += 1
                }
                trimmed.startsWith("## ") -> {
                    MarkdownLine(trimmed.removePrefix("## "), color = AccentHotPink, style = MaterialTheme.typography.titleLarge, weight = FontWeight.ExtraBold)
                    index += 1
                }
                trimmed.startsWith("# ") -> {
                    MarkdownLine(trimmed.removePrefix("# "), color = AccentHotPink, style = MaterialTheme.typography.headlineSmall, weight = FontWeight.Black)
                    index += 1
                }
                trimmed.startsWith(">") -> {
                    val quote = trimmed.removePrefix(">").trim()
                    QuoteBlock(quote, color, style)
                    index += 1
                }
                isUnorderedList(trimmed) -> {
                    ListLine(marker = "•", text = trimmed.drop(2).trim(), color = color, style = style)
                    index += 1
                }
                isOrderedList(trimmed) -> {
                    val marker = trimmed.takeWhile { it.isDigit() } + "."
                    val text = trimmed.substringAfter(".").trim()
                    ListLine(marker = marker, text = text, color = color, style = style)
                    index += 1
                }
                else -> {
                    val paragraph = buildString {
                        append(trimmed)
                        index += 1
                        while (index < lines.size) {
                            val next = lines[index].trim()
                            if (
                                next.isBlank() ||
                                next.startsWith("#") ||
                                next.startsWith(">") ||
                                next.startsWith("```") ||
                                isUnorderedList(next) ||
                                isOrderedList(next)
                            ) {
                                break
                            }
                            append('\n')
                            append(next)
                            index += 1
                        }
                    }
                    MarkdownLine(paragraph, color = color, style = style)
                }
            }
        }
    }
}

@Composable
private fun MarkdownLine(
    text: String,
    color: Color,
    style: TextStyle,
    weight: FontWeight? = null
) {
    Text(
        text = markdownInline(text, color),
        style = style,
        fontWeight = weight,
        color = color
    )
}

@Composable
private fun ListLine(marker: String, text: String, color: Color, style: TextStyle) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(marker, color = AccentHotPink, style = style, fontWeight = FontWeight.Bold)
        Text(markdownInline(text, color), color = color, style = style, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun QuoteBlock(text: String, color: Color, style: TextStyle) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SoftBlush.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(PrimaryPink, RoundedCornerShape(999.dp))
                .padding(horizontal = 2.dp)
        ) {
            Text(" ", style = style)
        }
        Text(markdownInline(text, color), color = color.copy(alpha = 0.82f), style = style, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CloudWhite.copy(alpha = 0.88f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            code,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun isUnorderedList(line: String): Boolean =
    (line.startsWith("- ") || line.startsWith("* ")) && line.length > 2

private fun isOrderedList(line: String): Boolean {
    val dot = line.indexOf(". ")
    return dot > 0 && line.take(dot).all(Char::isDigit)
}

private fun markdownInline(text: String, baseColor: Color) = buildAnnotatedString {
    var index = 0
    while (index < text.length) {
        when {
            text.startsWith("**", index) -> {
                val end = text.indexOf("**", index + 2)
                if (end > index) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor))
                    append(text.substring(index + 2, end))
                    pop()
                    index = end + 2
                } else {
                    append(text[index++])
                }
            }
            text[index] == '*' -> {
                val end = text.indexOf('*', index + 1)
                if (end > index) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseColor))
                    append(text.substring(index + 1, end))
                    pop()
                    index = end + 1
                } else {
                    append(text[index++])
                }
            }
            text[index] == '`' -> {
                val end = text.indexOf('`', index + 1)
                if (end > index) {
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            color = AccentHotPink,
                            background = CloudWhite.copy(alpha = 0.9f)
                        )
                    )
                    append(text.substring(index + 1, end))
                    pop()
                    index = end + 1
                } else {
                    append(text[index++])
                }
            }
            else -> append(text[index++])
        }
    }
}

