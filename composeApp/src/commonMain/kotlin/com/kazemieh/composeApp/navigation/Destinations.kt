package com.kazemieh.composeApp.navigation

import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.ic_navigation_home
import fintrack.core.designsystem.generated.resources.ic_navigation_report
import fintrack.core.designsystem.generated.resources.ic_navigation_setting
import fintrack.core.designsystem.generated.resources.navigation_home
import fintrack.core.designsystem.generated.resources.navigation_report
import fintrack.core.designsystem.generated.resources.navigation_setting
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
        Screen.Dashboard
    ),

    REPORT(
        Res.drawable.ic_navigation_report,
        Res.string.navigation_report,
        Screen.Report
    ),

    SETTING(
        Res.drawable.ic_navigation_setting,
        Res.string.navigation_setting,
        Screen.Setting
    );


}