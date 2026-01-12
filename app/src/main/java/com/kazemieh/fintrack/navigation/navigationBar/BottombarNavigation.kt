package com.kazemieh.fintrack.navigation.navigationBar

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kazemieh.dashboard.DashboardScreen
import com.kazemieh.setting.SettingScreen
import com.tosantechno.filter.ReportScreen
import kotlinx.serialization.Serializable


@Serializable
object
BottomBarGraph

@Serializable
object
Dashboard

@Serializable
object
Report

@Serializable
object
Setting


fun NavGraphBuilder.bottomBarNavGraph(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onBackPressed: () -> Unit
) {
    navigation<BottomBarGraph>(startDestination = Dashboard) {

        composable<Dashboard> { backStackEntry ->
            DashboardScreen(snackbarHostState = snackbarHostState)
        }

        composable<Report> { backStackEntry ->
            ReportScreen(snackbarHostState = snackbarHostState)
        }

        composable<Setting> { backStackEntry ->
            SettingScreen(snackbarHostState = snackbarHostState)
        }

    }
}