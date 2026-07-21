package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.AiAdvisorRepository
import com.kazemieh.domain.repository.AiConfig

/** Reads and persists the cloud AI advisor configuration (user-supplied endpoint / key / model). */
class AiConfigUseCase(
    private val repository: AiAdvisorRepository
) {
    fun get(): AiConfig = repository.getConfig()

    fun save(config: AiConfig) = repository.saveConfig(config)
}
