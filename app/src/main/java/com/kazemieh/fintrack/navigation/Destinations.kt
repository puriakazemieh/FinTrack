package com.kazemieh.fintrack.navigation

import com.kazemieh.fintrack.R
import com.kazemieh.fintrack.navigation.navigationBar.Dashboard
import com.kazemieh.fintrack.navigation.navigationBar.Report
import kotlinx.serialization.Serializable

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
    );

    val path: String get() = route::class.java.name

}