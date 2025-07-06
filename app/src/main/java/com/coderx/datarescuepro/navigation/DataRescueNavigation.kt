package com.coderx.datarescuepro.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coderx.datarescuepro.ui.screens.dashboard.DashboardScreen
import com.coderx.datarescuepro.ui.screens.recovery.RecoveryScreen
import com.coderx.datarescuepro.ui.screens.results.ResultsScreen
import com.coderx.datarescuepro.ui.screens.scan.ScanScreen
import com.coderx.datarescuepro.ui.screens.splash.SplashScreen

@Composable
fun DataRescueNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        
        composable("dashboard") {
            DashboardScreen(
                onNavigateToScan = { fileType ->
                    navController.navigate("scan/$fileType")
                }
            )
        }
        
        composable("scan/{fileType}") { backStackEntry ->
            val fileType = backStackEntry.arguments?.getString("fileType") ?: "all"
            ScanScreen(
                fileType = fileType,
                onNavigateToResults = { scanResults ->
                    navController.navigate("results")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("results") {
            ResultsScreen(
                onNavigateToRecovery = { selectedFiles ->
                    navController.navigate("recovery")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("recovery") {
            RecoveryScreen(
                onNavigateBack = {
                    navController.popBackStack("dashboard", false)
                }
            )
        }
    }
}