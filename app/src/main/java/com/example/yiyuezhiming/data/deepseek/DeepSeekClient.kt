package com.example.yiyuezhiming.data.deepseek

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class DeepSeekMessage(
    val role: String,
    val content: String
)

@Singleton
class DeepSeekClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    suspend fun chatCompletion(
        apiKey: String,
        messages: List<DeepSeekMessage>,
        temperature: Double,
        maxTokens: Int
    ): DeepSeekResult<String> = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("model", MODEL)
            .put("temperature", temperature)
            .put("max_tokens", maxTokens)
            .put(
                "messages",
                JSONArray().also { array ->
                    messages.forEach { message ->
                        array.put(JSONObject().put("role", message.role).put("content", message.content))
                    }
                }
            )

        val request = Request.Builder()
            .url(CHAT_COMPLETIONS_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext DeepSeekResult.Failure(mapHttpError(response.code, body))
                }
                val content = runCatching {
                    JSONObject(body)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()
                }.getOrNull()
                if (content.isNullOrBlank()) {
                    DeepSeekResult.Failure(DeepSeekError.ParseError())
                } else {
                    DeepSeekResult.Success(content)
                }
            }
        } catch (_: IOException) {
            DeepSeekResult.Failure(DeepSeekError.NetworkError())
        } catch (_: Exception) {
            DeepSeekResult.Failure(DeepSeekError.ParseError())
        }
    }

    private fun mapHttpError(code: Int, body: String): DeepSeekError {
        val message = runCatching {
            JSONObject(body).optJSONObject("error")?.optString("message")
        }.getOrNull().orEmpty()
        return when (code) {
            401, 403 -> DeepSeekError.Unauthorized(message.ifBlank { "API Key 无效或无权限" })
            429 -> DeepSeekError.RateLimited(message.ifBlank { "请求太频繁了，稍后再试一下" })
            in 500..599 -> DeepSeekError.ServerError(message.ifBlank { "DeepSeek 服务暂时不可用" })
            else -> DeepSeekError.ServerError(message.ifBlank { "DeepSeek 请求失败（$code）" })
        }
    }

    private companion object {
        const val MODEL = "deepseek-chat"
        const val CHAT_COMPLETIONS_URL = "https://api.deepseek.com/chat/completions"
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

