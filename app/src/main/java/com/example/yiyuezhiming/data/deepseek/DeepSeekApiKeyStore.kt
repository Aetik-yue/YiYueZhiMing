package com.example.yiyuezhiming.data.deepseek

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepSeekApiKeyStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "deepseek_secure_prefs",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getApiKey(): String = prefs.getString(KEY_API_KEY, null).orEmpty()

    fun hasApiKey(): Boolean = getApiKey().isNotBlank()

    fun saveApiKey(value: String) {
        prefs.edit().putString(KEY_API_KEY, value.trim()).apply()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
    }

    private companion object {
        const val KEY_API_KEY = "api_key"
    }
}

