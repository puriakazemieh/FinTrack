package com.kazemieh.fintrack.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kazemieh.fintrack.DashboardScreen
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


fun NavGraphBuilder.bottomBarNavGraph(
    navController: NavHostController,
    onBackPressed: () -> Unit
) {
    navigation<BottomBarGraph>(startDestination = Dashboard) {

        composable<Dashboard> { backStackEntry ->
            DashboardScreen()
        }

        composable<Report> { backStackEntry ->
            DashboardScreen()
        }

    }
}