package com.kazemieh.fintrack.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.kazemieh.fintrack.navigation.navigationBar.BottomBarGraph
import com.kazemieh.fintrack.navigation.navigationBar.bottomBarNavGraph
import kotlinx.serialization.Serializable

@Serializable
object Splash

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
) {


    NavHost(navController = navController, startDestination = BottomBarGraph, modifier = modifier) {
//        composable<Splash> {
//            SplashRoute {
//                navController.navigate(Dashboard) {
//                    popUpTo(Splash) { inclusive = true }
//                }
//            }
//        }

        bottomBarNavGraph(navController,snackbarHostState) { }

    }
}
