package com.kazemieh.composeApp.navigation.navigationBar

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kazemieh.dashboard.DashboardScreen
import com.kazemieh.filter.ReportScreen
import com.kazemieh.composeApp.navigation.Screen
import com.kazemieh.setting.SettingScreen


fun NavGraphBuilder.bottomBarNavGraph(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onBackPressed: () -> Unit
) {
    navigation<Screen.BottomBarGraph>(startDestination = Screen.Dashboard) {

        composable<Screen.Dashboard> { backStackEntry ->
            DashboardScreen(snackbarHostState = snackbarHostState)
        }

        composable<Screen.Report> { backStackEntry ->
            ReportScreen(snackbarHostState = snackbarHostState)
        }

        composable<Screen.Setting> { backStackEntry ->
            SettingScreen(snackbarHostState = snackbarHostState)
        }

    }
}