package com.kazemieh.network.service

import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.llm.OpenAILLMProvider
import ai.koog.prompt.message.MessagePart

/**
 * AI chat backed by Koog, pointed at any OpenAI-compatible endpoint. The user supplies the base
 * URL / API key / model from settings — nothing is hardcoded. Any failure returns null so the
 * caller falls back to the local rule-based advisor.
 *
 * The base URL should be the API root (e.g. https://router.bynara.id); Koog appends the standard
 * "v1/chat/completions" path itself.
 */
class AiChatService {

    suspend fun chat(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String
    ): String? {
        return try {
            val client = OpenAILLMClient(
                apiKey = apiKey,
                settings = OpenAIClientSettings(baseUrl = baseUrl.trimEnd('/')),
                httpClientFactory = KtorKoogHttpClient.Factory()
            )
            val llModel = LLModel(
                provider = OpenAILLMProvider,
                id = model,
                capabilities = listOf(
                    LLMCapability.Completion,
                    LLMCapability.OpenAIEndpoint.Completions
                )
            )
            val request = prompt("fintrack-advisor") {
                system(systemPrompt)
                user(userPrompt)
            }
            val response = client.execute(request, llModel, emptyList())
            response.parts
                .filterIsInstance<MessagePart.Text>()
                .joinToString(separator = "") { it.text }
                .trim()
                .takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }
}
