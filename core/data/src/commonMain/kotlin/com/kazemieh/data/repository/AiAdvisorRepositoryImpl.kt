package com.kazemieh.data.repository

import com.kazemieh.domain.repository.AiAdvisorRepository
import com.kazemieh.domain.repository.AiConfig
import com.kazemieh.domain.repository.PreferenceRepository
import com.kazemieh.network.service.AiChatService
import com.kazemieh.preferences.FinTrackPreferences

class AiAdvisorRepositoryImpl(
    private val aiChatService: AiChatService,
    private val preferenceRepository: PreferenceRepository
) : AiAdvisorRepository {

    override fun isCloudEnabled(): Boolean {
        if (!preferenceRepository.getBoolean(FinTrackPreferences.PREF_AI_CLOUD_ENABLED, false)) return false
        val base = preferenceRepository.getString(FinTrackPreferences.PREF_AI_BASE_URL, DEFAULT_BASE_URL)
        val key = preferenceRepository.getString(FinTrackPreferences.PREF_AI_API_KEY, "")
        return base.isNotBlank() && key.isNotBlank()
    }

    override fun getConfig(): AiConfig = AiConfig(
        enabled = preferenceRepository.getBoolean(FinTrackPreferences.PREF_AI_CLOUD_ENABLED, false),
        baseUrl = preferenceRepository.getString(FinTrackPreferences.PREF_AI_BASE_URL, DEFAULT_BASE_URL),
        apiKey = preferenceRepository.getString(FinTrackPreferences.PREF_AI_API_KEY, ""),
        model = preferenceRepository.getString(FinTrackPreferences.PREF_AI_MODEL, DEFAULT_MODEL)
    )

    override fun saveConfig(config: AiConfig) {
        preferenceRepository.putBoolean(FinTrackPreferences.PREF_AI_CLOUD_ENABLED, config.enabled)
        preferenceRepository.putString(FinTrackPreferences.PREF_AI_BASE_URL, config.baseUrl.trim())
        preferenceRepository.putString(FinTrackPreferences.PREF_AI_API_KEY, config.apiKey.trim())
        preferenceRepository.putString(FinTrackPreferences.PREF_AI_MODEL, config.model.trim())
    }

    override suspend fun generateInsight(context: String): String? {
        if (!isCloudEnabled()) return null
        val base = preferenceRepository.getString(FinTrackPreferences.PREF_AI_BASE_URL, DEFAULT_BASE_URL)
            .ifBlank { DEFAULT_BASE_URL }
        val key = preferenceRepository.getString(FinTrackPreferences.PREF_AI_API_KEY, "")
        val model = preferenceRepository.getString(FinTrackPreferences.PREF_AI_MODEL, DEFAULT_MODEL)
            .ifBlank { DEFAULT_MODEL }
        return aiChatService.chat(base, key, model, SYSTEM_PROMPT, context)
    }

    private companion object {
        // Defaults target the Nara router (OpenAI-compatible). Koog appends "v1/chat/completions",
        // so this is the API root. The user still supplies their own API key in-app; it is never
        // stored in source.
        const val DEFAULT_BASE_URL = "https://router.bynara.id"
        const val DEFAULT_MODEL = "glm-5.2-free"
        const val SYSTEM_PROMPT =
            "تو یک مشاور مالی شخصی فارسی‌زبان هستی. بر اساس خلاصهٔ مالی کاربر، یک تحلیل کوتاه، " +
                "دوستانه و عملی به زبان فارسی بنویس. حداکثر چهار جمله. به اعداد واقعی اشاره کن و از " +
                "توصیه‌های کلی و کلیشه‌ای پرهیز کن."
    }
}
