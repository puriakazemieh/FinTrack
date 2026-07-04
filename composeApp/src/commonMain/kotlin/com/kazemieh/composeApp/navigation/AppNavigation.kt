package com.kazemieh.composeApp.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kazemieh.composeApp.navigation.navigationBar.bottomBarNavGraph
import com.kazemieh.backup_export.ui.ExportSheet
import com.kazemieh.onboarding.ui.OnboardingScreen
import com.kazemieh.sync.ui.BackupRestoreScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Screen = Screen.BottomBarGraph
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            composable<Screen.Onboarding> {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Screen.BottomBarGraph) {
                            popUpTo(Screen.Onboarding) { inclusive = true }
                        }
                    }
                )
            }
            composable<Screen.BackupRestore> {
                BackupRestoreScreen(
                    onBackClick = { navController.popBackStack() },
                    exportSheet = { onDismiss -> ExportSheet(onClose = onDismiss) }
                )
            }
            bottomBarNavGraph(navController)
        }
    }
}
