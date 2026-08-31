package com.kazemieh.goals.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Goal
import com.kazemieh.common.model.GoalTemplate
import com.kazemieh.domain.usecase.GoalUseCases
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class AddGoalViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val goalUseCases: GoalUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(AddGoalState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddGoalEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            goalUseCases.observeGoalTemplates().collect { templates ->
                _state.update { it.copy(templates = templates) }
            }
        }
        viewModelScope.launch {
            goalUseCases.observeGoalBaskets().collect { baskets ->
                _state.update { it.copy(baskets = baskets) }
            }
        }
    }

    fun onIntent(intent: AddGoalIntent) {
        when (intent) {
            is AddGoalIntent.InitialData -> {
                if (intent.goal != null) {
                    intent.goal.let { goal ->
                        _state.update {
                            it.copy(
                                id = goal.id,
                                name = goal.name,
                                targetAmount = goal.targetAmount.toString(),
                                savedAmount = goal.savedAmount.toString(),
                                startDate = goal.startDate,
                                endDate = goal.endDate,
                                recurrence = goal.recurrence,
                                monthlyTarget = goal.monthlyTarget.toString(),
                                type = goal.type,
                                category = goal.category,
                                priority = goal.priority,
                                description = goal.description,
                                templateId = goal.templateId,
                                basketId = goal.basketId
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            startDate = Clock.System.now().toEpochMilliseconds()
                        )
                    }
                }
            }
            is AddGoalIntent.UpdateName -> _state.update { it.copy(name = intent.name) }
            is AddGoalIntent.UpdateTargetAmount -> _state.update { it.copy(targetAmount = intent.amount) }
            is AddGoalIntent.UpdateSavedAmount -> _state.update { it.copy(savedAmount = intent.amount) }
            is AddGoalIntent.UpdateStartDate -> _state.update { it.copy(startDate = intent.date) }
            is AddGoalIntent.UpdateEndDate -> {
                _state.update { it.copy(endDate = intent.date) }
                calculateMonthlyTarget()
            }
            is AddGoalIntent.UpdateRecurrence -> _state.update { it.copy(recurrence = intent.recurrence) }
            is AddGoalIntent.UpdateMonthlyTarget -> _state.update { it.copy(monthlyTarget = intent.amount) }
            is AddGoalIntent.UpdateType -> _state.update { it.copy(type = intent.type) }
            is AddGoalIntent.UpdateCategory -> _state.update { it.copy(category = intent.category) }
            is AddGoalIntent.UpdatePriority -> _state.update { it.copy(priority = intent.priority) }
            is AddGoalIntent.UpdateDescription -> _state.update { it.copy(description = intent.description) }
            is AddGoalIntent.UpdateTemplateId -> _state.update { it.copy(templateId = intent.id) }
            is AddGoalIntent.UpdateBasketId -> _state.update { it.copy(basketId = intent.id) }
            
            is AddGoalIntent.AddTemplate -> viewModelScope.launch { goalUseCases.addGoalTemplate(intent.template) }
            is AddGoalIntent.DeleteTemplate -> viewModelScope.launch { goalUseCases.deleteGoalTemplate(intent.id) }
            is AddGoalIntent.AddBasket -> viewModelScope.launch { goalUseCases.addGoalBasket(intent.basket) }
            is AddGoalIntent.DeleteBasket -> viewModelScope.launch { goalUseCases.deleteGoalBasket(intent.id) }
            
            AddGoalIntent.SaveGoal -> saveGoal()
        }
    }

    private fun calculateMonthlyTarget() {
        val currentState = _state.value
        val target = currentState.targetAmount.toLongOrNull() ?: 0L
        val saved = currentState.savedAmount.toLongOrNull() ?: 0L
        val remaining = target - saved
        
        if (remaining > 0 && currentState.endDate != null) {
            val months = (currentState.endDate - currentState.startDate) / (1000L * 60 * 60 * 24 * 30)
            if (months > 0) {
                val monthly = remaining / months
                _state.update { it.copy(monthlyTarget = monthly.toString()) }
            }
        }
    }

    private fun saveGoal() {
        viewModelScope.launch {
            val currentState = _state.value
            val goal = Goal(
                id = currentState.id,
                name = currentState.name,
                targetAmount = currentState.targetAmount.toLongOrNull() ?: 0L,
                savedAmount = currentState.savedAmount.toLongOrNull() ?: 0L,
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                recurrence = currentState.recurrence,
                monthlyTarget = currentState.monthlyTarget.toLongOrNull() ?: 0L,
                type = currentState.type,
                category = currentState.category,
                priority = currentState.priority,
                description = currentState.description,
                templateId = currentState.templateId,
                basketId = currentState.basketId
            )
            
            if (goal.id == 0L) {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.GoalCreated)
                goalUseCases.addGoal(goal)
            } else {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.GoalUpdated)
                goalUseCases.updateGoal(goal)
            }
            _effect.send(AddGoalEffect.GoalSaved)
        }
    }
}
