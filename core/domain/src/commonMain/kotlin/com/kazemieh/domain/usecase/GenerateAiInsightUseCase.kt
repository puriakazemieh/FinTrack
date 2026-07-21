package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.AiAdvisorRepository

class GenerateAiInsightUseCase(
    private val repository: AiAdvisorRepository
) {
    fun isEnabled(): Boolean = repository.isCloudEnabled()

    suspend operator fun invoke(context: String): String? = repository.generateInsight(context)
}
