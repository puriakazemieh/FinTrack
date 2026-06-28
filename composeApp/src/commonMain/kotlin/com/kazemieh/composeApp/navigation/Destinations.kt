package com.kazemieh.composeApp.navigation

import fintrack.core.designsystem.generated.resources.*
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

@Serializable
enum class Destinations(
    val icon: DrawableResource,
    val label: StringResource,
    val route: Screen
) {

    DASHBOARD(
        Res.drawable.ic_navigation_home,
        Res.string.navigation_home,
        Screen.Dashboard()
    ),

    TRANSACTIONS(
        Res.drawable.ic_navigation_report,
        Res.string.navigation_transactions,
        Screen.Transactions()
    ),

    TOOLS(
        Res.drawable.ic_navigation_setting, // Placeholder icon
        Res.string.navigation_tools,
        Screen.Tools
    ),

    PROFILE(
        Res.drawable.ic_navigation_setting,
        Res.string.navigation_profile,
        Screen.Profile
    );

}
