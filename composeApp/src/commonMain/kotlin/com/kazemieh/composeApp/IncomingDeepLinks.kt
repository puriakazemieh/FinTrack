package com.kazemieh.composeApp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

/** Connects warm platform deliveries after the navigation graph and app lock are ready. */
@Composable
internal expect fun BindIncomingDeepLinks(navController: NavHostController)
