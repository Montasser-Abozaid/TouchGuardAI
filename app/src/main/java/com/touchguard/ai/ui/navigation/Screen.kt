package com.touchguard.ai.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Diagnostics : Screen("diagnostics")
    data object TouchGridTest : Screen("diag_touch_grid")
    data object MultiTouchTest : Screen("diag_multitouch")
    data object LinearityTest : Screen("diag_linearity")
    data object DeadPixelTest : Screen("diag_dead_pixel")
    data object AutonomousTest : Screen("diag_autonomous")
    data object HeatmapTest : Screen("diag_heatmap")
    data object DeadZones : Screen("dead_zones")
    data object InteractiveCalibration : Screen("interactive_calibration")
    data object Profiles : Screen("profiles")
    data object Reports : Screen("reports")
    data object Simulator : Screen("simulator")
}
