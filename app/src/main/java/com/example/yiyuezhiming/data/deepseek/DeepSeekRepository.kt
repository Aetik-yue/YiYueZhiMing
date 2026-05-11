package com.example.yiyuezhiming.data.deepseek

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepSeekRepository @Inject constructor(
    private val keyStore: DeepSeekApiKeyStore,
    private val client: DeepSeekClient,
    private val logger: DeepSeekRequestLogger
) {
    fun hasApiKey(): Boolean = keyStore.hasApiKey()

    fun saveApiKey(value: String) = keyStore.saveApiKey(value)

    fun clearApiKey() = keyStore.clearApiKey()

    fun currentApiKeyMask(): String {
        val key = keyStore.getApiKey()
        return if (key.length <= 8) "" else "${key.take(4)}****${key.takeLast(4)}"
    }

    suspend fun complete(
        module: String,
        messages: List<DeepSeekMessage>,
        temperature: Double = 0.7,
        maxTokens: Int = 900
    ): DeepSeekResult<String> {
        val validation = validate(messages, temperature, maxTokens)
        if (validation != null) return DeepSeekResult.Failure(validation)

        val apiKey = keyStore.getApiKey()
        if (apiKey.isBlank()) return DeepSeekResult.Failure(DeepSeekError.MissingApiKey)

        val startedAt = System.currentTimeMillis()
        val result = client.chatCompletion(apiKey, messages, temperature, maxTokens)
        when (result) {
            is DeepSeekResult.Success -> logger.log(module, startedAt, "success")
            is DeepSeekResult.Failure -> logger.log(module, startedAt, "failure", result.error.userMessage)
        }
        return result
    }

    private fun validate(messages: List<DeepSeekMessage>, temperature: Double, maxTokens: Int): DeepSeekError? {
        if (messages.isEmpty()) return DeepSeekError.Validation("请求内容不能为空")
        if (messages.any { it.content.isBlank() }) return DeepSeekError.Validation("请求内容不能为空")
        if (messages.sumOf { it.content.length } > 12_000) return DeepSeekError.Validation("上下文太长了，请缩短后再试")
        if (temperature !in 0.0..2.0) return DeepSeekError.Validation("temperature 超出范围")
        if (maxTokens !in 64..4_096) return DeepSeekError.Validation("maxTokens 超出范围")
        return null
    }
}

