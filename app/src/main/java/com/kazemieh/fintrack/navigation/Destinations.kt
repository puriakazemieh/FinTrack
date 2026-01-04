package com.kazemieh.fintrack.navigation

import com.kazemieh.fintrack.R
import com.kazemieh.fintrack.navigation.navigationBar.Dashboard
import com.kazemieh.fintrack.navigation.navigationBar.Report
import com.kazemieh.fintrack.navigation.navigationBar.Setting
import kotlinx.serialization.Serializable

@Serializable
enum class Destinations(
    val icon: Int,
    val label: Int,
    val route: @Serializable Any
) {

    DASHBOARD(
        R.drawable.ic_navigation_home,
        com.kazemieh.designsystem.R.string.navigation_home,
        Dashboard
    ),

    REPORT(
        R.drawable.ic_navigation_report,
        com.kazemieh.designsystem.R.string.navigation_report,
        Report
    ),

    SETTING(
        R.drawable.ic_navigation_setting,
        com.kazemieh.designsystem.R.string.navigation_setting,
        Setting
    );

    val path: String get() = route::class.java.name

}