
package com.coderx.datarescuepro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coderx.datarescuepro.ui.screens.MainScreen
import com.coderx.datarescuepro.ui.screens.SplashScreen
import com.coderx.datarescuepro.ui.screens.ScanScreen
import com.coderx.datarescuepro.ui.screens.ResultsScreen

@Composable
fun DataRescueNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        
        composable("main") {
            MainScreen(
                onNavigateToScan = {
                    navController.navigate("scan")
                }
            )
        }
        
        composable("scan") {
            ScanScreen(
                onNavigateToResults = {
                    navController.navigate("results")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("results") {
            ResultsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
