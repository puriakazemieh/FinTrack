package com.kazemieh.composeApp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import com.kazemieh.designsystem.component.SnackbarController
import com.kazemieh.composeApp.navigation.AppNavHost
import com.kazemieh.composeApp.navigation.Screen
import com.kazemieh.composeApp.navigation.navigationBar.FintrackNavigationBar
import com.kazemieh.designsystem.component.model.resolveString
import kotlinx.coroutines.flow.collectLatest


@Composable
fun FinTrackHost(
    startDestination: Screen = Screen.BottomBarGraph
) {

    val navController = rememberNavController()

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route.orEmpty()
    val showBottomBar = remember(currentRoute) {
        val topLevelScreens = listOf(
            Screen.Dashboard::class.qualifiedName,
            Screen.Tools::class.qualifiedName,
            Screen.Transactions::class.qualifiedName,
            Screen.Profile::class.qualifiedName
        )
        topLevelScreens.any { currentRoute.contains(it ?: "") }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        SnackbarController.events.collectLatest { event ->
            snackbarHostState.showSnackbar(event.message.resolveString())
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
                    startDestination = startDestination
                )

                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = fadeIn(animationSpec = tween(400)) +
                            slideInVertically(
                                animationSpec = tween(400, easing = FastOutSlowInEasing),
                                initialOffsetY = { it }
                            ),
                    exit = fadeOut(animationSpec = tween(400)) +
                            slideOutVertically(
                                animationSpec = tween(400, easing = FastOutSlowInEasing),
                                targetOffsetY = { it }
                            ),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    FintrackNavigationBar(
                        navController = navController,
                        onFabClick = { /* TODO: Global Add Transaction */ }
                    )
                }

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
