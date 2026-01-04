package com.kazemieh.fintrack

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kazemieh.fintrack.navigation.AppNavHost
import com.kazemieh.fintrack.navigation.Destinations
import com.kazemieh.fintrack.navigation.navigationBar.FintrackNavigationBar


@Composable
fun FinTrackHost() {

    val navController = rememberNavController()

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route.orEmpty()
    val topLevelRoutes = Destinations.entries.map { it.path }
    val showBottomBar = currentRoute in topLevelRoutes

    val snackbarHostState = remember { SnackbarHostState() }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                FintrackNavigationBar(navController = navController)
            }
        ) { innerPadding ->
            Box {
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                    snackbarHostState = snackbarHostState
                )
                Box(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    SnackbarHost(
                        hostState = snackbarHostState,
                    )
                }
            }
        }
    }

}

