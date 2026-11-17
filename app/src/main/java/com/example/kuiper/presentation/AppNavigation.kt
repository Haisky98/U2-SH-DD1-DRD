package com.example.kuiper.presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController

object Destinations {
    const val MAIN = "home_screen"
    const val DETAIL = "detail_screen"
}

@Composable
fun AppNavigation() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Destinations.MAIN
    ) {
        composable(Destinations.MAIN) {
            HomeScreen(
                onNavigateToDetail = {
                    navController.navigate(Destinations.DETAIL)
                }
            )
        }

        composable(Destinations.DETAIL) {
            DetailScreen()
        }
    }
}