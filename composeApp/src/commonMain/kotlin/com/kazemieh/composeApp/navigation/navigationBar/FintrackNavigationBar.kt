package com.kazemieh.composeApp.navigation.navigationBar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
    val currentDestination = navBackStackEntry?.destination

    val selectedDestination by remember(currentDestination) {
        derivedStateOf {
            val route = currentDestination?.route.toString()
            when {
                route.contains("Dashboard") -> Destinations.DASHBOARD
                route.contains("Transactions") -> Destinations.TRANSACTIONS
                route.contains("Tools") -> Destinations.TOOLS
                route.contains("Profile") -> Destinations.PROFILE
                else -> Destinations.DASHBOARD
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            .height(84.dp), // Height allows FAB to protrude
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Frosted Pill Background Layer
        val backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
        val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp) // The actual pill height
                .clip(CircleShape)
                .background(backgroundColor)
                .border(
                    1.dp,
                    borderColor,
                    CircleShape
                )
                .glassBlur(30.dp) 
        )

        // 2. Content Layer (Icons Row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp) // Matches the pill height
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Destinations.entries.forEachIndexed { index, destination ->
                // Center slot for FAB
                if (index == 2) {
                    Spacer(modifier = Modifier.weight(1.5f)) // Reserved space for the protruding FAB
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
                            if (!isSelected) {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(destination.icon),
                        contentDescription = stringResource(destination.label),
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 3. Central Floating Action Button (Protruding)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter) // Align to top of the 84dp container
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
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
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
