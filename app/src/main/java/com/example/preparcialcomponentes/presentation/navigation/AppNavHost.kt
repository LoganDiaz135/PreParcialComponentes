package com.example.preparcialcomponentes.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.preparcialcomponentes.presentation.AppViewModelFactory
import com.example.preparcialcomponentes.presentation.screens.home.HomeScreen
import com.example.preparcialcomponentes.presentation.screens.home.HomeViewModel
import com.example.preparcialcomponentes.presentation.screens.plans.PlansScreen
import com.example.preparcialcomponentes.presentation.screens.plans.PlansViewModel

/**
 * Grafo principal de navegación Compose.
 *
 * Utiliza [AppViewModelFactory] para suministrar dependencias a los ViewModels.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    factory: AppViewModelFactory = AppViewModelFactory()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = factory)
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToPlans = {
                    navController.navigate(Screen.Plans.route)
                }
            )
        }

        composable(Screen.Plans.route) {
            val plansViewModel: PlansViewModel = viewModel(factory = factory)
            PlansScreen(
                viewModel = plansViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
