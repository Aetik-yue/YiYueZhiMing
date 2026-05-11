package com.example.yiyuezhiming.data.deepseek

sealed class DeepSeekError(open val userMessage: String) {
    data object MissingApiKey : DeepSeekError("请先在设置里填写 DeepSeek API Key")
    data class Validation(override val userMessage: String) : DeepSeekError(userMessage)
    data class Unauthorized(override val userMessage: String = "API Key 无效或无权限") : DeepSeekError(userMessage)
    data class RateLimited(override val userMessage: String = "请求太频繁了，稍后再试一下") : DeepSeekError(userMessage)
    data class ServerError(override val userMessage: String = "DeepSeek 服务暂时不可用") : DeepSeekError(userMessage)
    data class NetworkError(override val userMessage: String = "网络连接失败，请检查网络后重试") : DeepSeekError(userMessage)
    data class ParseError(override val userMessage: String = "AI 回复解析失败，请稍后再试") : DeepSeekError(userMessage)
}

sealed class DeepSeekResult<out T> {
    data class Success<T>(val value: T) : DeepSeekResult<T>()
    data class Failure(val error: DeepSeekError) : DeepSeekResult<Nothing>()
}

