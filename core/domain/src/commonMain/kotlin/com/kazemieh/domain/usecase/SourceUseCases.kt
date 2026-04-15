package com.kazemieh.domain.usecase

data class SourceUseCases(
    val updateSourceUseCase: UpdateSourceUseCase,
    val addSource: AddSourceUseCase,
    val observeSourceUseCase: ObserveSourceUseCase,
)
