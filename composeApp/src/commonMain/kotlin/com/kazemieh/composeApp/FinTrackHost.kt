package com.kazemieh.composeApp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kazemieh.common.SnackbarController
import com.kazemieh.composeApp.navigation.AppNavHost
import com.kazemieh.composeApp.navigation.navigationBar.FintrackNavigationBar
import kotlinx.coroutines.flow.collectLatest


@Composable
fun FinTrackHost() {

    val navController = rememberNavController()

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route.orEmpty()
//    val topLevelRoutes = Destinations.entries.map { it.path }
//    val showBottomBar = currentRoute in topLevelRoutes

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        SnackbarController.events.collectLatest { event ->
            snackbarHostState.showSnackbar(event.message)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
            ) {
                // Main content fills the whole screen
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.fillMaxSize(),
                    snackbarHostState = snackbarHostState
                )

                FintrackNavigationBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    navController = navController,
                    onFabClick = { /* TODO: Global Add Transaction */ }
                )

                // Snackbar above the navigation bar
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp)
                )
            }
        }
    }
}
