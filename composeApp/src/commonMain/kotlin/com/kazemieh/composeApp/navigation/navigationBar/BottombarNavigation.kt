package com.kazemieh.composeApp.navigation.navigationBar

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.composeApp.navigation.Screen
import com.kazemieh.dashboard.DashboardScreen
import com.kazemieh.notifications.ui.NotificationSettingsScreen
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
                onNavigateToSearch = { navController.navigate(Screen.Search) }
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
            ToolsScreen(snackbarHostState = snackbarHostState)
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
