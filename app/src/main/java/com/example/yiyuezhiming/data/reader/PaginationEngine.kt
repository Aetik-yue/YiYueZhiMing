package com.example.yiyuezhiming.data.reader

import android.text.TextPaint
import com.example.yiyuezhiming.model.Line
import com.example.yiyuezhiming.model.Page

class PaginationEngine(
    private val pageWidth: Float = 720f,
    private val pageHeight: Float = 980f,
    private val lineSpacing: Float = 10f,
    private val paragraphSpacing: Float = 16f
) {
    fun paginate(content: String, chapterIndex: Int, fontSize: Float = 42f): List<Page> {
        val paint = TextPaint().apply {
            isAntiAlias = true
            textSize = fontSize
        }
        val maxLinesPerPage = ((pageHeight / (fontSize + lineSpacing)).toInt()).coerceAtLeast(6)
        val lines = mutableListOf<Line>()
        val paragraphs = content.replace("\r\n", "\n").split("\n")
        var offset = 0
        paragraphs.forEach { paragraph ->
            val text = paragraph.trim()
            if (text.isBlank()) {
                lines += Line("", offset, offset)
                offset += paragraph.length + 1
                return@forEach
            }
            var start = 0
            while (start < text.length) {
                var end = start + 1
                var lastBreak = end
                while (end <= text.length) {
                    val candidate = text.substring(start, end)
                    if (paint.measureText(candidate) > pageWidth) break
                    if (end == text.length || text[end - 1].isWhitespace() || text[end - 1].isCjk()) {
                        lastBreak = end
                    }
                    end++
                }
                val lineEnd = if (end > text.length) text.length else lastBreak.coerceAtLeast(start + 1)
                val lineText = text.substring(start, lineEnd).trim()
                lines += Line(lineText, offset + start, offset + lineEnd)
                start = lineEnd
                while (start < text.length && text[start].isWhitespace()) start++
            }
            offset += paragraph.length + 1
            repeat((paragraphSpacing / (fontSize + lineSpacing)).toInt().coerceAtLeast(0)) {
                lines += Line("", offset, offset)
            }
        }
        if (lines.isEmpty()) return listOf(Page(chapterIndex, 0, 0, 0, listOf(Line("这一章暂时没有可显示内容。", 0, 0))))
        return lines.chunked(maxLinesPerPage).mapIndexed { index, chunk ->
            Page(
                chapterIndex = chapterIndex,
                pageIndex = index,
                startOffset = chunk.firstOrNull()?.startOffset ?: 0,
                endOffset = chunk.lastOrNull()?.endOffset ?: 0,
                lines = chunk
            )
        }
    }

    private fun Char.isCjk(): Boolean = this in '\u4E00'..'\u9FFF' || this in '\u3040'..'\u30FF' || this in '\uAC00'..'\uD7AF'
}
