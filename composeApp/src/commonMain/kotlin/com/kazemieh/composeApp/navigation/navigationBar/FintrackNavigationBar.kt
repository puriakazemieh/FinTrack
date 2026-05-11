package com.kazemieh.composeApp.navigation.navigationBar

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kazemieh.designsystem.component.FintrackLabelSmallText
//import com.kazemieh.composeApp.navigation.Destinations
//import fintrack.composeapp.generated.resources.ic_navigation_report
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FintrackNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
//    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
//    val selectedDestination by remember {
//        derivedStateOf {
//            val route = currentDestination?.route.toString()
//            when {
//                route.contains(Destinations.DASHBOARD.route.toString()) -> Destinations.DASHBOARD
//                route.contains(Destinations.REPORT.route.toString()) -> Destinations.REPORT
//                route.contains(Destinations.SETTING.route.toString()) -> Destinations.SETTING
//                else -> Destinations.DASHBOARD
//            }
//        }
//    }
//    NavigationBar(modifier = modifier) {
//        Destinations.entries.forEach { destination ->
//            val selected = selectedDestination.route == destination.route
//            NavigationBarItem(
//                selected = selected,
//                label = {
//                    FintrackLabelSmallText(stringResource(destination.label))
//                },
//                icon = {
////                    Icon(
//////                        painter = painterResource(destination.icon),
////                        painter = painterResource(fintrack.composeapp.generated.resources.Res.drawable.ic_navigation_report),
//////                        imageVector = ImageVector.vectorResource(id = destination.icon),
////                        contentDescription = stringResource(destination.label)
////                    )
//                },
//                onClick = {
//                    if (!selected) {
//                        navController.navigate(destination.route) {
//                            popUpTo(navController.graph.findStartDestination().id) {
//                                saveState = true
//                            }
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    }
//                },
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = MaterialTheme.colorScheme.primary,
//                    selectedTextColor = MaterialTheme.colorScheme.primary,
//                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                    indicatorColor = Color.Transparent
//                ),
//            )
//        }
//    }
}