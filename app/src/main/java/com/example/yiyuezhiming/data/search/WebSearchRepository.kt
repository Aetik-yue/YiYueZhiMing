package com.example.yiyuezhiming.data.search

import java.io.IOException
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

data class WebSearchResult(
    val title: String,
    val snippet: String,
    val url: String
)

@Singleton
class WebSearchRepository @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    suspend fun search(query: String, limit: Int = 4): List<WebSearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()
        val encoded = URLEncoder.encode(trimmed.take(160), "UTF-8")
        val request = Request.Builder()
            .url("https://duckduckgo.com/html/?q=$encoded")
            .header("User-Agent", "Mozilla/5.0")
            .get()
            .build()
        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                parseResults(response.body?.string().orEmpty()).take(limit)
            }
        } catch (_: IOException) {
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseResults(html: String): List<WebSearchResult> {
        val blocks = Regex("""(?s)<div class="result.*?</div>\s*</div>""").findAll(html)
        return blocks.mapNotNull { match ->
            val block = match.value
            val titleMatch = Regex("""class="result__a"[^>]*href="([^"]+)"[^>]*>(.*?)</a>""").find(block)
            val snippetMatch = Regex("""class="result__snippet"[^>]*>(.*?)</a>""").find(block)
                ?: Regex("""class="result__snippet"[^>]*>(.*?)</div>""").find(block)
            val title = titleMatch?.groupValues?.getOrNull(2)?.cleanHtml().orEmpty()
            val url = titleMatch?.groupValues?.getOrNull(1)?.cleanDuckDuckGoUrl().orEmpty()
            val snippet = snippetMatch?.groupValues?.getOrNull(1)?.cleanHtml().orEmpty()
            if (title.isBlank() || url.isBlank()) null else WebSearchResult(title, snippet, url)
        }.distinctBy { it.url }.toList()
    }

    private fun String.cleanHtml(): String = this
        .replace(Regex("<.*?>"), "")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#x27;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun String.cleanDuckDuckGoUrl(): String {
        val value = replace("&amp;", "&")
        val uddg = Regex("""uddg=([^&]+)""").find(value)?.groupValues?.getOrNull(1)
        return runCatching {
            java.net.URLDecoder.decode(uddg ?: value, "UTF-8")
        }.getOrElse { value }
    }
}
