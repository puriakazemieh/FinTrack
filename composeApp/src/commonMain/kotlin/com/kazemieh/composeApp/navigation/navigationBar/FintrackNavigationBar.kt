package com.kazemieh.composeApp.navigation.navigationBar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kazemieh.composeApp.navigation.Destinations
import com.kazemieh.designsystem.component.glass.glassBlur
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FintrackNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onFabClick: () -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val selectedDestination by remember(navBackStackEntry) {
        derivedStateOf {
            val currentRoute = navBackStackEntry?.destination?.route.toString()
            val isSearchOpen = currentRoute.contains("Search", ignoreCase = true)

            val routeToAnalyze = if (isSearchOpen) {
                navController.previousBackStackEntry?.destination?.route.toString()
            } else {
                currentRoute
            }

            when {
                routeToAnalyze.contains("Dashboard", ignoreCase = true) -> Destinations.DASHBOARD
                routeToAnalyze.contains("Transactions", ignoreCase = true) -> Destinations.TRANSACTIONS
                routeToAnalyze.contains("Tools", ignoreCase = true) -> Destinations.TOOLS
                routeToAnalyze.contains("Profile", ignoreCase = true) -> Destinations.PROFILE
                else -> Destinations.DASHBOARD
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
            .height(80.dp),
        contentAlignment = Alignment.BottomCenter
    ) {

        val backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(
                    0.5.dp,
                    borderColor,
                    CircleShape
                )
                .glassBlur(20.dp)
        )

        // 2. Content Layer (Icons Row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp) // Matches the new slimmer pill height
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Destinations.entries.forEachIndexed { index, destination ->
                // Center slot for FAB
                if (index == 2) {
                    Spacer(modifier = Modifier.weight(1.3f))
                }

                val isSelected = selectedDestination == destination
                val color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable {
                            val currentRoute = navController.currentBackStackEntry?.destination?.route.toString()
                            val isSearchOpen = currentRoute.contains("Search", ignoreCase = true)

                            if (!isSelected) {
                                if (isSearchOpen) {
                                    navController.popBackStack()
                                }
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else if (isSearchOpen) {
                                // If the same tab is clicked while Search is open, close Search
                                navController.popBackStack()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(destination.icon),
                        contentDescription = stringResource(destination.label),
                        tint = color,
                        modifier = Modifier.size(22.dp) // Slightly smaller icons
                    )
                }
            }
        }

        // 3. Central Floating Action Button (Protruding)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(56.dp) // Slightly adjusted size
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        )
                    )
                )
                .clickable(onClick = onFabClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
