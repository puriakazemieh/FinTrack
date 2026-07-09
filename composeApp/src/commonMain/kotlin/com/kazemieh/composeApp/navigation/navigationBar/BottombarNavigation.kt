package com.kazemieh.composeApp.navigation.navigationBar

import com.kazemieh.notes.ui.NotesListScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import androidx.navigation.navDeepLink
import com.kazemieh.asset.ui.add.AddAssetScreen
import com.kazemieh.asset.ui.list.AssetsListScreen
import com.kazemieh.budget.ui.list.BudgetScreen
import com.kazemieh.category.ui.list.CategoriesScreen
import com.kazemieh.check.ui.list.CheckListScreen
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.composeApp.navigation.Screen
import com.kazemieh.dashboard.DashboardScreen
import com.kazemieh.debt.ui.list.DebtsScreen
import com.kazemieh.financialsource.ui.list.SourcesScreen
import com.kazemieh.fixed_expense.ui.list.FixedExpenseListScreen
import com.kazemieh.goals.presentation.GoalScreen
import com.kazemieh.gamification.ui.AchievementsScreen
import com.kazemieh.ai_insights.ui.AIAdvisorScreen
import com.kazemieh.installment.ui.list.InstallmentsScreen
import com.kazemieh.notes.ui.edit.NoteEditScreen
import com.kazemieh.notifications.ui.NotificationSettingsScreen
import com.kazemieh.person.ui.detail.PersonDetailScreen
import com.kazemieh.person.ui.list.PersonsScreen
import com.kazemieh.profile.CurrencySettingsScreen
import com.kazemieh.profile.ManageToolsScreen
import com.kazemieh.profile.ProfileEditScreen
import com.kazemieh.profile.ProfileScreen
import com.kazemieh.profile.ThemeSettingsScreen
import com.kazemieh.search.ui.SearchScreen
import com.kazemieh.shopping.ui.ShoppingListScreen
import com.kazemieh.tag.ui.list.TagsScreen
import com.kazemieh.tools.ToolsScreen
import com.kazemieh.transactions.TransactionsScreen
import com.kazemieh.utilities.ui.converter.CurrencyConverterScreen
import com.kazemieh.utilities.ui.calendar.FinancialCalendarScreen
import com.kazemieh.utilities.ui.events.EventsScreen
import com.kazemieh.utilities.ui.faq.FAQScreen
import com.kazemieh.utilities.ui.fx.FxRatesScreen
import com.kazemieh.utilities.ui.news.ArticleReaderScreen
import com.kazemieh.utilities.ui.news.NewsListScreen
import com.kazemieh.utilities.ui.support.SupportScreen

fun NavGraphBuilder.bottomBarNavGraph(navController: NavHostController) {
    val navigateToTransactions: (Any?) -> Unit = { data ->
        navController.popBackStack()
        val route = when (data) {
            is Category -> Screen.Transactions(categoryId = data.id)
            is Source -> Screen.Transactions(sourceId = data.id)
            is Tag -> Screen.Transactions(tagId = data.id)
            is Person -> Screen.Transactions(personId = data.id)
            is com.kazemieh.common.model.TransactionType -> Screen.Transactions(transactionType = data)
            is String -> Screen.Transactions(query = data)
            is Boolean -> Screen.Transactions(resetFilters = data)
            else -> Screen.Transactions()
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
        }
    }

    navigation<Screen.BottomBarGraph>(startDestination = Screen.Dashboard()) {

        composable<Screen.Dashboard>(
            deepLinks = listOf(
                navDeepLink { uriPattern = "fintrack://dashboard?showAddTransaction={showAddTransaction}" }
            )
        ) { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.Dashboard>()
            DashboardScreen(
                showAddTransaction = args.showAddTransaction,
                onNavigateToTransactions = navigateToTransactions,
                onNavigateToSearch = { navController.navigate(Screen.Search) },
                onNavigateToBudget = { navController.navigate(Screen.Budget) },
                onNavigateToGoal = { navController.navigate(Screen.Goal) },
                onNavigateToCheck = { navController.navigate(Screen.Check) },
                onNavigateToFixedExpense = { navController.navigate(Screen.FixedExpense) },
                onNavigateToInstallment = { navController.navigate(Screen.Installment) },
                onNavigateToAIAdvisor = { navController.navigate(Screen.AIAdvisor) },
                onNavigateToAssets = { navController.navigate(Screen.Assets) },
                onNavigateToAchievements = { navController.navigate(Screen.Achievements) },
                onNavigateToShopping = { navController.navigate(Screen.Shopping) },
                onNavigateToNotes = { navController.navigate(Screen.Notes) },
                onNavigateToProfileEdit = { navController.navigate(Screen.EditProfile) },
                onNavigateToNotifications = { navController.navigate(Screen.NotificationSettings) }
            )
        }

        composable<Screen.Transactions> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.Transactions>()
            TransactionsScreen(
                resetFilters = args.resetFilters,
                categoryId = args.categoryId,
                sourceId = args.sourceId,
                tagId = args.tagId,
                personId = args.personId,
                transactionType = args.transactionType,
                onNavigateToSearch = { navController.navigate(Screen.Search) }
            )
        }

        composable<Screen.Tools> { backStackEntry ->
            ToolsScreen(
                onNavigateToBudget = { navController.navigate(Screen.Budget) },
                onNavigateToGoal = { navController.navigate(Screen.Goal) },
                onNavigateToInstallment = { navController.navigate(Screen.Installment) },
                onNavigateToPerson = { navController.navigate(Screen.Person) },
                onNavigateToDebt = { navController.navigate(Screen.Debt) },
                onNavigateToCheck = { navController.navigate(Screen.Check) },
                onNavigateToFixedExpense = { navController.navigate(Screen.FixedExpense) },
                onNavigateToAssets = { navController.navigate(Screen.Assets) },
                onNavigateToAchievements = { navController.navigate(Screen.Achievements) },
                onNavigateToShopping = { navController.navigate(Screen.Shopping) },
                onNavigateToNotes = { navController.navigate(Screen.Notes) },
                onNavigateToSource = { navController.navigate(Screen.Sources) },
                onNavigateToCategory = { navController.navigate(Screen.Categories) },
                onNavigateToTag = { navController.navigate(Screen.Tags) },
                onNavigateToAIAdvisor = { navController.navigate(Screen.AIAdvisor) },
                onNavigateToFxRates = { navController.navigate(Screen.FxRates) },
                onNavigateToCurrencyConverter = { navController.navigate(Screen.CurrencyConverter) },
                onNavigateToNews = { navController.navigate(Screen.News) },
                onNavigateToFAQ = { navController.navigate(Screen.FAQ) },
                onNavigateToSupport = { navController.navigate(Screen.Support) },
                onNavigateToEvents = { navController.navigate(Screen.Events) },
                onNavigateToFinancialCalendar = { navController.navigate(Screen.FinancialCalendar) }
            )
        }

        composable<Screen.FxRates> {
            FxRatesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.CurrencyConverter> {
            CurrencyConverterScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.News> {
            NewsListScreen(
                onArticleClick = { id -> navController.navigate(Screen.ArticleReader(id)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.ArticleReader> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.ArticleReader>()
            ArticleReaderScreen(
                articleId = args.articleId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.FAQ> {
            FAQScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.Support> {
            SupportScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.Events> {
            EventsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.FinancialCalendar> {
            FinancialCalendarScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.AIAdvisor> {
            AIAdvisorScreen(
                onBack = { navController.popBackStack() },
                onAddTransaction = {
                    navController.navigate(Screen.Dashboard(showAddTransaction = true)) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Screen.Achievements> {
            AchievementsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Sources> {
            SourcesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTransactions = { source ->
                    navigateToTransactions(source)
                }
            )
        }

        composable<Screen.Categories> {
            CategoriesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTransactions = { category ->
                    navigateToTransactions(category)
                }
            )
        }

        composable<Screen.Tags> {
            TagsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTransactions = { tag ->
                    navigateToTransactions(tag)
                }
            )
        }

        composable<Screen.Budget> {
            BudgetScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTransactions = { category ->
                    navigateToTransactions(category)
                }
            )
        }

        composable<Screen.Goal> {
            GoalScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Installment> {
            InstallmentsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Person> {
            PersonsScreen(
                onNavigateToDetail = { person ->
                    navController.navigate(Screen.PersonDetail(person.id ?: 0L))
                },
                onBack = { navController.popBackStack() },
                onNavigateToTransactions = { person ->
                    navigateToTransactions(person)
                }
            )
        }

        composable<Screen.PersonDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.PersonDetail>()
            PersonDetailScreen(
                personId = args.personId,
                onBack = { navController.popBackStack() },
                addDebtSheet = { personId, debtId, onDismiss ->
                    com.kazemieh.debt.ui.add.AddDebtBottomSheet(
                        onDismiss = onDismiss,
                        personId = personId,
                        debtId = debtId
                    )
                },
                addTransactionSheet = { onDismiss ->
                    com.kazemieh.transaction.ui.add.AddTransactionBottomSheet(
                        onDismiss = onDismiss,
                        transactionAdded = onDismiss
                    )
                }
            )
        }

        composable<Screen.Debt> {
            DebtsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPersonDetail = { personId ->
                    navController.navigate(Screen.PersonDetail(personId))
                }
            )
        }

        composable<Screen.Check> {
            CheckListScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.FixedExpense> {
            FixedExpenseListScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Assets> {
            AssetsListScreen(
                onAddAsset = { id: Long? -> navController.navigate(Screen.AddAsset(id)) },
                onBack = { navController.popBackStack() },
                onNavigateToTransactions = { query: String ->
                    navigateToTransactions(query)
                }
            )
        }

        composable<Screen.AddAsset> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.AddAsset>()
            AddAssetScreen(
                assetId = args.assetId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Shopping> {
            ShoppingListScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Notes> {
            NotesListScreen(
                onAddNote = { navController.navigate(Screen.NoteEdit(0L)) },
                onEditNote = { id: Long -> navController.navigate(Screen.NoteEdit(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.NoteEdit> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.NoteEdit>()
            NoteEditScreen(
                noteId = args.noteId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Profile> { backStackEntry ->
            ProfileScreen(
                onNavigateToThemeSettings = { navController.navigate(Screen.ThemeSettings) },
                onNavigateToCurrencySettings = { navController.navigate(Screen.CurrencySettings) },
                onNavigateToProfileEdit = { navController.navigate(Screen.EditProfile) },
                onNavigateToNotifications = { navController.navigate(Screen.NotificationSettings) },
                onNavigateToFAQ = { navController.navigate(Screen.FAQ) },
                onNavigateToSupport = { navController.navigate(Screen.Support) },
                onNavigateToBackupRestore = { navController.navigate(Screen.BackupRestore) },
                onNavigateToManageTools = { navController.navigate(Screen.ManageTools) },
                onLoggedOut = {
                    navController.navigate(Screen.Onboarding) {
                        popUpTo(Screen.BottomBarGraph) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Screen.EditProfile> { backStackEntry ->
            ProfileEditScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.NotificationSettings> {
            NotificationSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.ManageTools> {
            ManageToolsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.ThemeSettings> { backStackEntry ->
            ThemeSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.CurrencySettings> { backStackEntry ->
            CurrencySettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Search> {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onNavigateToCategory = { cat -> navigateToTransactions(cat) },
                onNavigateToSource = { src -> navigateToTransactions(src) },
                onNavigateToPerson = { p -> navigateToTransactions(p) },
                onNavigateToTag = { t -> navigateToTransactions(t) },
                onNavigateToTransactionType = { type -> navigateToTransactions(type) }
            )
        }

    }
}
