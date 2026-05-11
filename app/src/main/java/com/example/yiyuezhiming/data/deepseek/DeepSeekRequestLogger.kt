package com.example.yiyuezhiming.data.deepseek

import com.example.yiyuezhiming.data.local.DeepSeekLogDao
import com.example.yiyuezhiming.data.local.DeepSeekRequestLogEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepSeekRequestLogger @Inject constructor(
    private val dao: DeepSeekLogDao
) {
    suspend fun log(module: String, startedAt: Long, status: String, errorSummary: String? = null) {
        dao.insert(
            DeepSeekRequestLogEntity(
                module = module,
                startedAt = startedAt,
                durationMs = System.currentTimeMillis() - startedAt,
                status = status,
                errorSummary = errorSummary?.take(180)
            )
        )
    }
}

