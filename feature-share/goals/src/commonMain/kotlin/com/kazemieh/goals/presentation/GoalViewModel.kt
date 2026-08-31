package com.kazemieh.goals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.*
import com.kazemieh.domain.usecase.AssetUseCases
import com.kazemieh.domain.usecase.GoalUseCases
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import com.kazemieh.preferences.FinTrackPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GoalViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val goalUseCases: GoalUseCases,
    private val assetUseCases: AssetUseCases,
    private val transactionUseCases: TransactionUseCaseGroup,
    private val preferenceUseCases: PreferenceUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(GoalState())
    val state = _state.asStateFlow()

    private val _effect = Channel<GoalEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.GoalListViewed)
        onIntent(GoalIntent.LoadGoals)
        loadRoundUpSettings()
    }

    private fun loadRoundUpSettings() {
        _state.update {
            it.copy(
                isRoundUpEnabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_ROUNDUP_ENABLED, false),
                roundUpGoalId = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_ROUNDUP_GOAL_ID, "").toLongOrNull(),
                roundUpUnit = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_ROUNDUP_UNIT, "5000").toLongOrNull() ?: 5000L
            )
        }
    }

    fun onIntent(intent: GoalIntent) {
        when (intent) {
            GoalIntent.LoadGoals -> observeData()
            is GoalIntent.SelectTab -> _state.update { it.copy(currentTab = intent.tab) }
            is GoalIntent.UpdateSearchQuery -> _state.update { it.copy(searchQuery = intent.query) }
            is GoalIntent.DeleteGoal -> deleteGoal(intent.id)
            is GoalIntent.ShowAddGoal -> {
                _state.update { it.copy(isAddGoalShow = !it.isAddGoalShow, selectedGoal = intent.goal) }
            }
            is GoalIntent.AddAmountToGoal -> addAmountToGoal(intent.id, intent.amount)
            GoalIntent.ToggleRoundUpSettings -> _state.update { it.copy(showRoundUpSettings = !it.showRoundUpSettings) }
            GoalIntent.ToggleRoundUpEnabled -> {
                val newValue = !_state.value.isRoundUpEnabled
                preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_ROUNDUP_ENABLED, newValue)
                _state.update { it.copy(isRoundUpEnabled = newValue) }
            }
            is GoalIntent.SetRoundUpGoal -> {
                preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_ROUNDUP_GOAL_ID, intent.goalId.toString())
                _state.update { it.copy(roundUpGoalId = intent.goalId) }
            }
            is GoalIntent.SetRoundUpUnit -> {
                preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_ROUNDUP_UNIT, intent.unit.toString())
                _state.update { it.copy(roundUpUnit = intent.unit) }
            }
            is GoalIntent.AddBasket -> viewModelScope.launch { goalUseCases.addGoalBasket(intent.basket) }
            is GoalIntent.UpdateBasket -> viewModelScope.launch { goalUseCases.updateGoalBasket(intent.basket) }
            is GoalIntent.DeleteBasket -> viewModelScope.launch { goalUseCases.deleteGoalBasket(intent.id) }
            is GoalIntent.AddTemplate -> viewModelScope.launch { goalUseCases.addGoalTemplate(intent.template) }
            is GoalIntent.UpdateTemplate -> viewModelScope.launch { goalUseCases.updateGoalTemplate(intent.template) }
            is GoalIntent.DeleteTemplate -> viewModelScope.launch { goalUseCases.deleteGoalTemplate(intent.id) }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            combine(
                goalUseCases.observeGoals(),
                goalUseCases.observeGoalBaskets(),
                goalUseCases.observeGoalTemplates(),
                assetUseCases.observeAssets(),
                transactionUseCases.observeSourcesUseCase(),
                goalUseCases.calculateFreedomStage()
            ) { flows ->
                val goals = flows[0] as List<Goal>
                val baskets = flows[1] as List<GoalBasket>
                val templates = flows[2] as List<GoalTemplate>
                val assets = flows[3] as List<Asset>
                val sources = flows[4] as List<Source>
                val stage = flows[5] as FreedomStage
                
                val totalSaved = goals.sumOf { it.savedAmount }
                val totalTarget = goals.sumOf { it.targetAmount }
                
                val items = mutableListOf<BasketData>()
                
                if (baskets.isEmpty()) {
                    val security = sources.sumOf { it.balance.toLong() } + 
                            goals.filter { it.type == GoalType.EMERGENCY_FUND }.sumOf { it.savedAmount }
                    
                    val growth = assets.sumOf { it.totalCurrentValue } + 
                            goals.filter { it.type == GoalType.INVESTMENT }.sumOf { it.savedAmount }
                    
                    val dream = goals.filter { 
                        it.type == GoalType.SAVINGS || it.type == GoalType.BIG_PURCHASE 
                    }.sumOf { it.savedAmount }
                    
                    val total = security + growth + dream
                    
                    items.add(BasketData(name = "Security", amount = security, percent = if(total > 0) (security * 100 / total).toInt() else 0, colorId = 1))
                    items.add(BasketData(name = "Growth", amount = growth, percent = if(total > 0) (growth * 100 / total).toInt() else 0, colorId = 2))
                    items.add(BasketData(name = "Dream", amount = dream, percent = if(total > 0) (dream * 100 / total).toInt() else 0, colorId = 3))
                } else {
                    val basketAmounts = baskets.map { b ->
                        goals.filter { it.basketId == b.id }.sumOf { it.savedAmount }
                    }
                    val total = basketAmounts.sum()
                    
                    baskets.forEachIndexed { index, b ->
                        val amount = basketAmounts[index]
                        items.add(BasketData(
                            id = b.id,
                            name = b.name,
                            amount = amount,
                            percent = if(total > 0) (amount * 100 / total).toInt() else 0,
                            colorId = b.colorId
                        ))
                    }
                }
                
                _state.update {
                    it.copy(
                        goals = goals,
                        templates = templates,
                        baskets = baskets,
                        basketItems = items,
                        totalSavedAmount = totalSaved,
                        totalTargetAmount = totalTarget,
                        freedomStage = stage,
                        isLoading = false
                    )
                }
            }.collectLatest { }
        }
    }

    private fun deleteGoal(id: Long) {
        viewModelScope.launch {
            analytics.track(com.kazemieh.common.analytics.ProductEvent.GoalDeleted)
            goalUseCases.deleteGoal(id)
        }
    }

    private fun addAmountToGoal(id: Long, amount: Long) {
        viewModelScope.launch {
            val goal = goalUseCases.getGoalById(id)
            if (goal != null) {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.GoalUpdated)
                val updatedGoal = goal.copy(savedAmount = goal.savedAmount + amount)
                goalUseCases.updateGoal(updatedGoal)
            }
        }
    }
}
