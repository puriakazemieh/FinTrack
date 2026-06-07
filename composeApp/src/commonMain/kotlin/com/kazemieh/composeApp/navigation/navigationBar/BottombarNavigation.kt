package com.kazemieh.composeApp.navigation.navigationBar

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.toRoute
import com.kazemieh.dashboard.DashboardScreen
import com.kazemieh.transactions.TransactionsScreen
import com.kazemieh.composeApp.navigation.Screen
import com.kazemieh.profile.ProfileScreen
import com.kazemieh.tools.ToolsScreen


fun NavGraphBuilder.bottomBarNavGraph(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onBackPressed: () -> Unit
) {
    navigation<Screen.BottomBarGraph>(startDestination = Screen.Dashboard) {

        composable<Screen.Dashboard> { backStackEntry ->
            DashboardScreen(
                snackbarHostState = snackbarHostState,
                onNavigateToTransactions = { reset ->
                    navController.navigate(Screen.Transactions(resetFilters = reset)) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Screen.Transactions> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.Transactions>()
            TransactionsScreen(
                snackbarHostState = snackbarHostState,
                resetFilters = args.resetFilters
            )
        }

        composable<Screen.Tools> { backStackEntry ->
            ToolsScreen(snackbarHostState = snackbarHostState)
        }

        composable<Screen.Profile> { backStackEntry ->
            ProfileScreen(snackbarHostState = snackbarHostState)
        }

    }
}
