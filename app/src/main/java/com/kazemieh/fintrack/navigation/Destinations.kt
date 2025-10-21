package com.kazemieh.fintrack.navigation

import com.kazemieh.fintrack.R
import kotlinx.serialization.Serializable

enum class Destinations(
    val icon: Int,
    val label: Int,
    val route: @Serializable Any
) {

    DASHBOARD(
        R.drawable.ic_navigation_home,
        R.string.navigation_home,
        Dashboard
    ),

    REPORT(
        R.drawable.ic_navigation_report,
        R.string.navigation_report,
        Report
    );

    val path: String get() = route::class.java.name

}