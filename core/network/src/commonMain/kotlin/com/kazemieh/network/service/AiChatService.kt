package com.kazemieh.network.service

import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.models.OpenAIChatCompletionResponse
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.llm.OpenAILLMProvider
import ai.koog.prompt.message.MessagePart
import com.kazemieh.common.ld
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

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
            val client = object : OpenAILLMClient(
                apiKey = apiKey,
                settings = OpenAIClientSettings(baseUrl = baseUrl.trimEnd('/')),
                httpClientFactory = KtorKoogHttpClient.Factory()
            ) {
                override fun decodeResponse(data: String): OpenAIChatCompletionResponse {
                    return try {
                        super.decodeResponse(data)
                    } catch (_: Exception) {
                        // Some OpenAI-compatible endpoints (like Mistral via router.bynara.id) 
                        // might omit 'created' or other required fields.
                        val json = Json { ignoreUnknownKeys = true }
                        val element = json.parseToJsonElement(data).jsonObject.toMutableMap()
                        if (!element.containsKey("created")) {
                            element["created"] = JsonPrimitive(0L)
                        }
                        if (!element.containsKey("id")) {
                            element["id"] = JsonPrimitive("fallback-id")
                        }
                        if (!element.containsKey("model")) {
                            element["model"] = JsonPrimitive(model)
                        }
                        if (!element.containsKey("object")) {
                            element["object"] = JsonPrimitive("chat.completion")
                        }
                        // Patch choices for missing finishReason
                        element["choices"]?.jsonArray?.let { choices ->
                            val patchedChoices = choices.map { choice ->
                                val choiceObj = choice.jsonObject.toMutableMap()
                                if (!choiceObj.containsKey("finishReason")) {
                                    // Map finish_reason to finishReason if it exists, otherwise use "stop"
                                    val reason = choiceObj["finish_reason"] ?: JsonPrimitive("stop")
                                    choiceObj["finishReason"] = reason
                                }
                                JsonObject(choiceObj)
                            }
                            element["choices"] = JsonArray(patchedChoices)
                        }
                        json.decodeFromJsonElement(
                            OpenAIChatCompletionResponse.serializer(),
                            JsonObject(element)
                        )
                    }
                }
            }
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
            val response = client.execute(request, llModel, emptyList()).ld("response")
            response.parts
                .filterIsInstance<MessagePart.Text>()
                .joinToString(separator = "") { it.text }
                .trim()
                .takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            e.ld("Exception")
            null
        }
    }
}
