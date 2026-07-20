package com.example.yiyuezhiming.data.reader

import com.example.yiyuezhiming.model.Line
import com.example.yiyuezhiming.model.Page
import org.json.JSONArray
import org.json.JSONObject

object PageJsonCodec {
    fun encode(pages: List<Page>): String {
        val array = JSONArray()
        pages.forEach { page ->
            array.put(
                JSONObject()
                    .put("chapterIndex", page.chapterIndex)
                    .put("pageIndex", page.pageIndex)
                    .put("startOffset", page.startOffset)
                    .put("endOffset", page.endOffset)
                    .put(
                        "lines",
                        JSONArray().also { lines ->
                            page.lines.forEach { line ->
                                lines.put(
                                    JSONObject()
                                        .put("text", line.text)
                                        .put("startOffset", line.startOffset)
                                        .put("endOffset", line.endOffset)
                                )
                            }
                        }
                    )
            )
        }
        return array.toString()
    }

    fun decode(value: String?): List<Page> {
        if (value.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(value)
            List(array.length()) { pageIndex ->
                val page = array.getJSONObject(pageIndex)
                val linesArray = page.getJSONArray("lines")
                Page(
                    chapterIndex = page.getInt("chapterIndex"),
                    pageIndex = page.getInt("pageIndex"),
                    startOffset = page.getInt("startOffset"),
                    endOffset = page.getInt("endOffset"),
                    lines = List(linesArray.length()) { lineIndex ->
                        val line = linesArray.getJSONObject(lineIndex)
                        Line(
                            text = line.getString("text"),
                            startOffset = line.getInt("startOffset"),
                            endOffset = line.getInt("endOffset")
                        )
                    }
                )
            }
        }.getOrDefault(emptyList())
    }
}
