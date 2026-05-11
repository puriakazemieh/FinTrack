package com.kazemieh.composeApp.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.kazemieh.composeApp.navigation.navigationBar.bottomBarNavGraph


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
) {

    NavHost(
        navController = navController,
        startDestination = Screen.BottomBarGraph,
        modifier = modifier
    ) {
        bottomBarNavGraph(navController, snackbarHostState) { }
    }
}
