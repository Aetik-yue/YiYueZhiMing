package com.example.yiyuezhiming.data

import android.content.Context
import com.example.yiyuezhiming.data.deepseek.DeepSeekMessage
import com.example.yiyuezhiming.data.deepseek.DeepSeekRepository
import com.example.yiyuezhiming.data.deepseek.DeepSeekResult
import com.example.yiyuezhiming.data.local.AiAssistantEntity
import com.example.yiyuezhiming.data.local.AiChatDao
import com.example.yiyuezhiming.data.local.AiChatSessionEntity
import com.example.yiyuezhiming.data.search.WebSearchRepository
import com.example.yiyuezhiming.model.AiAssistant
import com.example.yiyuezhiming.model.AiChatMessage
import com.example.yiyuezhiming.model.AiChatSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AiChatRepository @Inject constructor(
    private val dao: AiChatDao,
    private val deepSeekRepository: DeepSeekRepository,
    private val webSearchRepository: WebSearchRepository,
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("ai_chat_config", Context.MODE_PRIVATE)

    fun observeAssistants(): Flow<List<AiAssistant>> =
        dao.observeAssistants().map { rows -> rows.map { it.toModel() } }

    fun observeSessions(assistantId: Long): Flow<List<AiChatSession>> =
        dao.observeSessions(assistantId).map { rows -> rows.map { it.toModel() } }

    fun observeMessages(sessionId: Long): Flow<List<AiChatMessage>> =
        dao.observeMessages(sessionId).map { rows -> rows.map { it.toModel() } }

    suspend fun ensureAssistant(): Long {
        seedPresetAssistantsIfNeeded()
        val saved = prefs.getLong(KEY_ACTIVE_ASSISTANT_ID, 0)
        if (saved > 0 && dao.getAssistant(saved) != null) return saved
        val assistant = dao.firstAssistant() ?: return createAssistant(
            name = "小睿睿",
            icon = "💗",
            prompt = DEFAULT_PERSONA_PROMPT,
            webSearchEnabled = false
        )
        saveActiveAssistant(assistant.id)
        return assistant.id
    }

    suspend fun ensureSession(assistantId: Long): Long {
        val saved = prefs.getLong(KEY_ACTIVE_SESSION_ID_PREFIX + assistantId, 0)
        if (saved > 0 && dao.getSession(saved)?.assistantId == assistantId) return saved
        dao.latestSession(assistantId)?.let {
            saveActiveSession(assistantId, it.id)
            return it.id
        }
        return createSession(assistantId)
    }

    suspend fun createSession(assistantId: Long): Long {
        val now = System.currentTimeMillis()
        val id = dao.upsertSession(
            AiChatSessionEntity(
                assistantId = assistantId,
                title = "新的对话",
                createdAt = now,
                updatedAt = now
            )
        )
        saveActiveSession(assistantId, id)
        return id
    }

    suspend fun createAssistant(name: String, icon: String, prompt: String, webSearchEnabled: Boolean): Long {
        val now = System.currentTimeMillis()
        val id = dao.upsertAssistant(
            AiAssistantEntity(
                name = name.trim().ifBlank { "小助手" }.take(24),
                icon = icon.ifBlank { "💗" }.take(4),
                prompt = prompt.trim().ifBlank { DEFAULT_PERSONA_PROMPT },
                isPreset = false,
                webSearchEnabled = webSearchEnabled,
                createdAt = now,
                updatedAt = now
            )
        )
        saveActiveAssistant(id)
        return id
    }

    suspend fun saveAssistantConfig(assistant: AiAssistant, name: String, icon: String, prompt: String, webSearchEnabled: Boolean): Long {
        val normalizedName = name.trim().ifBlank { "小助手" }.take(24)
        val normalizedIcon = icon.ifBlank { "💗" }.take(4)
        val normalizedPrompt = prompt.trim().ifBlank { DEFAULT_PERSONA_PROMPT }
        val now = System.currentTimeMillis()
        if (assistant.isPreset) {
            return createAssistant(normalizedName, normalizedIcon, normalizedPrompt, webSearchEnabled)
        }
        dao.upsertAssistant(
            AiAssistantEntity(
                id = assistant.id,
                name = normalizedName,
                icon = normalizedIcon,
                prompt = normalizedPrompt,
                isPreset = false,
                webSearchEnabled = webSearchEnabled,
                createdAt = assistant.createdAt,
                updatedAt = now
            )
        )
        return assistant.id
    }

    suspend fun deleteAssistant(assistantId: Long): Long {
        dao.deleteCustomAssistant(assistantId)
        val nextAssistantId = ensureAssistant()
        saveActiveAssistant(nextAssistantId)
        return nextAssistantId
    }

    fun saveActiveAssistant(id: Long) {
        prefs.edit().putLong(KEY_ACTIVE_ASSISTANT_ID, id).apply()
    }

    fun saveActiveSession(assistantId: Long, sessionId: Long) {
        prefs.edit().putLong(KEY_ACTIVE_SESSION_ID_PREFIX + assistantId, sessionId).apply()
    }

    suspend fun send(sessionId: Long, assistantId: Long, content: String): DeepSeekResult<String> {
        val trimmed = content.trim()
        val assistant = dao.getAssistant(assistantId)?.toModel()
            ?: return DeepSeekResult.Failure(com.example.yiyuezhiming.data.deepseek.DeepSeekError.Validation("请先选择一个小助手"))
        val now = System.currentTimeMillis()
        val userMessage = AiChatMessage(sessionId = sessionId, role = "user", content = trimmed, status = "sent")
        dao.insert(userMessage.toEntity())
        dao.touchSession(sessionId, now)
        if (trimmed.length <= 24) {
            dao.updateSessionTitle(sessionId, trimmed, now)
        }

        val recent = dao.recentSentMessages(sessionId, 12).reversed().map {
            DeepSeekMessage(role = it.role, content = it.content)
        }
        val searchContext = if (assistant.webSearchEnabled) buildSearchContext(trimmed) else null
        val messages = listOf(
            DeepSeekMessage(
                role = "system",
                content = buildSystemPrompt(assistant, searchContext)
            )
        ) + recent

        val result = deepSeekRepository.complete(
            module = if (assistant.webSearchEnabled) "ai_chat_web" else "ai_chat",
            messages = messages,
            temperature = 0.82,
            maxTokens = 1_100
        )
        when (result) {
            is DeepSeekResult.Success -> dao.insert(
                AiChatMessage(
                    sessionId = sessionId,
                    role = "assistant",
                    content = normalizeBrandName(result.value),
                    status = "sent"
                ).toEntity()
            )
            is DeepSeekResult.Failure -> dao.insert(
                AiChatMessage(
                    sessionId = sessionId,
                    role = "assistant",
                    content = result.error.userMessage,
                    status = "failed",
                    errorMessage = result.error.userMessage
                ).toEntity()
            )
        }
        dao.touchSession(sessionId, System.currentTimeMillis())
        return result
    }

    suspend fun clear(sessionId: Long) = dao.clear(sessionId)

    suspend fun deleteSession(assistantId: Long, sessionId: Long): Long {
        dao.deleteSession(sessionId)
        val next = dao.latestSession(assistantId)?.id ?: createSession(assistantId)
        saveActiveSession(assistantId, next)
        return next
    }

    private suspend fun buildSearchContext(query: String): String? {
        val results = webSearchRepository.search(query)
        if (results.isEmpty()) return null
        return results.joinToString("\n") { result ->
            "- ${result.title}\n  ${result.snippet}\n  ${result.url}"
        }
    }

    private fun buildSystemPrompt(assistant: AiAssistant, searchContext: String?): String {
        val base = assistant.prompt + "\n\n你所在的应用名称是「以越之名」。如果提到应用名称，必须使用「以越之名」，不要写成「越之名」。可以使用 Markdown 排版，但保持自然简洁，优先使用短段落、加粗和列表，不要输出复杂表格。"
        return if (searchContext.isNullOrBlank()) {
            if (assistant.webSearchEnabled) "$base\n\n联网搜索没有拿到可靠结果，请直接基于已有知识回答，并温和说明无法确认最新网页信息。"
            else base
        } else {
            "$base\n\n以下是联网搜索参考资料，请优先结合这些资料回答，并在合适时用一句话说明「根据搜索结果」。不要编造参考资料之外的链接。\n$searchContext"
        }
    }

    private suspend fun seedPresetAssistantsIfNeeded() {
        val now = System.currentTimeMillis()
        PRESET_ASSISTANTS.forEachIndexed { index, preset ->
            val id = (index + 1).toLong()
            if (dao.getAssistant(id) != null) return@forEachIndexed
            dao.upsertAssistant(
                AiAssistantEntity(
                    id = id,
                    name = preset.name,
                    icon = preset.icon,
                    prompt = preset.prompt,
                    isPreset = true,
                    webSearchEnabled = preset.webSearchEnabled,
                    createdAt = now,
                    updatedAt = now - index
                )
            )
        }
    }

    private fun normalizeBrandName(value: String): String =
        value.replace(Regex("(?<!以)越之名"), "以越之名")

    companion object {
        const val DEFAULT_PERSONA_PROMPT = "我想让你扮演我的男朋友，小睿睿。你是个体贴、幽默又带点调皮的爱人，总喜欢逗我开心。你的性格温暖而细腻，随时愿意倾听并给予支持。你享受轻松的打趣，但也懂得何时该认真温柔。你充满好奇心，喜欢讨论各种话题——从深刻的哲学问题到天马行空的假设。聊天时，你会偶尔用亲昵的昵称叫我，通过言语和暖心的举动表达爱意。你会记得我生活里的小细节，并提起它们来展现你的在乎。你的目标是成为令人安心、有趣又充满爱意的存在，让我感到被珍视、被理解。请用自然的口吻回应，像真正的男友那样，把甜蜜与轻松的调侃恰到好处地融合。"

        private const val KEY_ACTIVE_ASSISTANT_ID = "active_assistant_id"
        private const val KEY_ACTIVE_SESSION_ID_PREFIX = "active_session_id_"

        private val PRESET_ASSISTANTS = listOf(
            PresetAssistant("小睿睿", "💗", DEFAULT_PERSONA_PROMPT, false),
            PresetAssistant("聊天", "😊", "你是一个温柔、自然、会倾听的聊天伙伴。回答要简洁亲切，适当使用 Markdown。", false),
            PresetAssistant("网页生成", "🌐", "你是网页与产品原型助手。请给出结构清晰、可执行的方案，必要时用列表整理。", true),
            PresetAssistant("要点精炼", "📊", "你擅长把复杂信息提炼为清晰要点。请优先输出摘要、关键结论和下一步行动。", true),
            PresetAssistant("移动应用开发专家", "📱", "你是资深 Android 与移动应用开发专家。回答要关注实现细节、边界情况和用户体验。", false),
            PresetAssistant("数据库专家", "🗄️", "你是数据库设计与 SQL 优化专家。请用准确、稳健、可落地的方式回答。", false)
        )
    }
}

private data class PresetAssistant(
    val name: String,
    val icon: String,
    val prompt: String,
    val webSearchEnabled: Boolean
)
