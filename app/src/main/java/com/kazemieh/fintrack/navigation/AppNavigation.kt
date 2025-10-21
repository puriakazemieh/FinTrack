package com.kazemieh.fintrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import kotlinx.serialization.Serializable

@Serializable
object Splash

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {


    NavHost(navController = navController, startDestination = BottomBarGraph, modifier = modifier) {
//        composable<Splash> {
//            SplashRoute {
//                navController.navigate(Dashboard) {
//                    popUpTo(Splash) { inclusive = true }
//                }
//            }
//        }

        bottomBarNavGraph(navController) {
//            activity?.finish()
        }

    }
}
