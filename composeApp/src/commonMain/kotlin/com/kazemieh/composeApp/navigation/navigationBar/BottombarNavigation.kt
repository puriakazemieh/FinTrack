package com.kazemieh.composeApp.navigation.navigationBar

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.kazemieh.asset.ui.add.AddAssetScreen
import com.kazemieh.asset.ui.list.AssetsListScreen
import com.kazemieh.budget.ui.list.BudgetScreen
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.composeApp.navigation.Screen
import com.kazemieh.check.ui.list.CheckListScreen
import com.kazemieh.dashboard.DashboardScreen
import com.kazemieh.debt.ui.list.DebtsScreen
import com.kazemieh.fixed_expense.ui.list.FixedExpenseListScreen
import com.kazemieh.installment.ui.list.InstallmentsScreen
import com.kazemieh.notifications.ui.NotificationSettingsScreen
import com.kazemieh.person.ui.detail.PersonDetailScreen
import com.kazemieh.person.ui.list.PersonsScreen
import com.kazemieh.profile.ProfileEditScreen
import com.kazemieh.profile.ProfileScreen
import com.kazemieh.profile.ThemeAndCurrencyScreen
import com.kazemieh.search.ui.SearchScreen
import com.kazemieh.tools.ToolsScreen
import com.kazemieh.transactions.TransactionsScreen


fun NavGraphBuilder.bottomBarNavGraph(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onBackPressed: () -> Unit
) {
    val navigateToTransactions: (Any?) -> Unit = { data ->
        navController.popBackStack()
        val route = when (data) {
            is Category -> Screen.Transactions(categoryId = data.id)
            is Source -> Screen.Transactions(sourceId = data.id)
            is Tag -> Screen.Transactions(tagId = data.id)
            is Person -> Screen.Transactions(personId = data.id)
            is com.kazemieh.common.model.TransactionType -> Screen.Transactions(transactionType = data)
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

    navigation<Screen.BottomBarGraph>(startDestination = Screen.Dashboard) {

        composable<Screen.Dashboard> { backStackEntry ->
            DashboardScreen(
                snackbarHostState = snackbarHostState,
                onNavigateToTransactions = navigateToTransactions,
                onNavigateToSearch = { navController.navigate(Screen.Search) },
                onNavigateToBudget = { navController.navigate(Screen.Budget) },
                onNavigateToCheck = { navController.navigate(Screen.Check) },
                onNavigateToFixedExpense = { navController.navigate(Screen.FixedExpense) }
            )
        }

        composable<Screen.Transactions> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.Transactions>()
            TransactionsScreen(
                snackbarHostState = snackbarHostState,
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
                snackbarHostState = snackbarHostState,
                onNavigateToBudget = { navController.navigate(Screen.Budget) },
                onNavigateToInstallment = { navController.navigate(Screen.Installment) },
                onNavigateToPerson = { navController.navigate(Screen.Person) },
                onNavigateToDebt = { navController.navigate(Screen.Debt) },
                onNavigateToCheck = { navController.navigate(Screen.Check) },
                onNavigateToFixedExpense = { navController.navigate(Screen.FixedExpense) }
            )
        }

        composable<Screen.Budget> {
            BudgetScreen(onBack = { navController.popBackStack() })
        }

        composable<Screen.Installment> {
            InstallmentsScreen(
                onAddInstallment = { /* I'll handle showing the bottom sheet in InstallmentsScreen */ }
            )
        }

        composable<Screen.Person> {
            PersonsScreen(
                snackbarHostState = snackbarHostState,
                onNavigateToDetail = { person ->
                    navController.navigate(Screen.PersonDetail(person.id ?: 0L))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.PersonDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.PersonDetail>()
            PersonDetailScreen(
                personId = args.personId,
                snackbarHostState = snackbarHostState,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Debt> {
            DebtsScreen(
                snackbarHostState = snackbarHostState,
                onBack = { navController.popBackStack() }
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
                onAddAsset = { navController.navigate(Screen.AddAsset) }
            )
        }

        composable<Screen.AddAsset> {
            AddAssetScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Profile> { backStackEntry ->
            ProfileScreen(
                onNavigateToThemeAndCurrency = { navController.navigate(Screen.ThemeAndCurrency) },
                onNavigateToProfileEdit = { navController.navigate(Screen.ProfileEdit) },
                onNavigateToNotifications = { navController.navigate(Screen.NotificationSettings) }
            )
        }

        composable<Screen.ProfileEdit> { backStackEntry ->
            ProfileEditScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.NotificationSettings> {
            NotificationSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.ThemeAndCurrency> { backStackEntry ->
            ThemeAndCurrencyScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Search> {
            SearchScreen(
                snackbarHostState = snackbarHostState,
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
