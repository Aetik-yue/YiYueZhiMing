package com.example.yiyuezhiming.data

import com.example.yiyuezhiming.data.deepseek.DeepSeekMessage
import com.example.yiyuezhiming.data.deepseek.DeepSeekRepository
import com.example.yiyuezhiming.data.deepseek.DeepSeekResult
import com.example.yiyuezhiming.data.local.AiChatDao
import com.example.yiyuezhiming.model.AiChatMessage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AiChatRepository @Inject constructor(
    private val dao: AiChatDao,
    private val deepSeekRepository: DeepSeekRepository
) {
    fun observeMessages(): Flow<List<AiChatMessage>> =
        dao.observeMessages().map { rows -> rows.map { it.toModel() } }

    suspend fun send(content: String): DeepSeekResult<String> {
        val userMessage = AiChatMessage(role = "user", content = content.trim(), status = "sent")
        dao.insert(userMessage.toEntity())

        val recent = dao.recentSentMessages(12).reversed().map {
            DeepSeekMessage(role = it.role, content = it.content)
        }
        val messages = listOf(
            DeepSeekMessage(
                role = "system",
                content = "你是以越之名 App 里的温柔 AI 聊天伙伴。回答要真诚、简洁、有陪伴感；遇到医疗、法律、投资等高风险问题时提醒用户寻求专业帮助。"
            )
        ) + recent

        val result = deepSeekRepository.complete(
            module = "ai_chat",
            messages = messages,
            temperature = 0.8,
            maxTokens = 900
        )
        when (result) {
            is DeepSeekResult.Success -> dao.insert(
                AiChatMessage(role = "assistant", content = result.value, status = "sent").toEntity()
            )
            is DeepSeekResult.Failure -> dao.insert(
                AiChatMessage(
                    role = "assistant",
                    content = result.error.userMessage,
                    status = "failed",
                    errorMessage = result.error.userMessage
                ).toEntity()
            )
        }
        return result
    }

    suspend fun clear() = dao.clear()
}

