package com.kazemieh.domain.repository

/** User-supplied configuration for the cloud AI advisor (OpenAI-compatible endpoint). */
data class AiConfig(
    val enabled: Boolean = false,
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = ""
)

/**
 * Optional cloud-backed financial advice. Backed by a user-configured, OpenAI-compatible endpoint;
 * stays disabled until the user supplies a base URL and API key in settings.
 */
interface AiAdvisorRepository {
    /** True only when the cloud advisor is enabled and fully configured. */
    fun isCloudEnabled(): Boolean

    /** Returns a short Persian insight for the given financial context, or null on any failure. */
    suspend fun generateInsight(context: String): String?

    fun getConfig(): AiConfig
    fun saveConfig(config: AiConfig)
}
