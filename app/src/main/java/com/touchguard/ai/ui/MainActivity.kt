package com.touchguard.ai.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.touchguard.ai.ui.dashboard.DashboardScreen
import com.touchguard.ai.ui.deadzone.DeadZoneManagerScreen
import com.touchguard.ai.ui.deadzone.InteractiveCalibrationScreen
import com.touchguard.ai.ui.diagnostics.AutonomousGhostDetectorScreen
import com.touchguard.ai.ui.diagnostics.DeadPixelTestScreen
import com.touchguard.ai.ui.diagnostics.DiagnosticsScreen
import com.touchguard.ai.ui.diagnostics.HeatmapCanvas
import com.touchguard.ai.ui.diagnostics.LineTestCanvas
import com.touchguard.ai.ui.diagnostics.MultiTouchCanvas
import com.touchguard.ai.ui.diagnostics.TouchGridCanvas
import com.touchguard.ai.ui.navigation.Screen
import com.touchguard.ai.ui.profiles.ProfilesScreen
import com.touchguard.ai.ui.reports.SystemResearchScreen
import com.touchguard.ai.ui.simulator.TouchSimulatorScreen
import com.touchguard.ai.ui.theme.DarkNavyBackground
import com.touchguard.ai.ui.theme.TouchGuardTheme
import com.touchguard.ai.viewmodel.DeadZoneViewModel
import com.touchguard.ai.viewmodel.DiagnosticsViewModel
import com.touchguard.ai.viewmodel.MainViewModel
import com.touchguard.ai.viewmodel.SimulatorViewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val diagnosticsViewModel: DiagnosticsViewModel by viewModels()
    private val deadZoneViewModel: DeadZoneViewModel by viewModels()
    private val simulatorViewModel: SimulatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TouchGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkNavyBackground
                ) {
                    TouchGuardAppNavigation(
                        mainViewModel = mainViewModel,
                        diagnosticsViewModel = diagnosticsViewModel,
                        deadZoneViewModel = deadZoneViewModel,
                        simulatorViewModel = simulatorViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun TouchGuardAppNavigation(
    mainViewModel: MainViewModel,
    diagnosticsViewModel: DiagnosticsViewModel,
    deadZoneViewModel: DeadZoneViewModel,
    simulatorViewModel: SimulatorViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = mainViewModel,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.Diagnostics.route) {
            DiagnosticsScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TouchGridTest.route) {
            TouchGridCanvas(
                viewModel = diagnosticsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MultiTouchTest.route) {
            MultiTouchCanvas(
                viewModel = diagnosticsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LinearityTest.route) {
            LineTestCanvas(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DeadPixelTest.route) {
            DeadPixelTestScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AutonomousTest.route) {
            AutonomousGhostDetectorScreen(
                viewModel = diagnosticsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.HeatmapTest.route) {
            HeatmapCanvas(
                viewModel = diagnosticsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DeadZones.route) {
            DeadZoneManagerScreen(
                viewModel = deadZoneViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.InteractiveCalibration.route) {
            InteractiveCalibrationScreen(
                viewModel = deadZoneViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profiles.route) {
            ProfilesScreen(
                viewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Simulator.route) {
            TouchSimulatorScreen(
                viewModel = simulatorViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reports.route) {
            SystemResearchScreen(
                viewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
