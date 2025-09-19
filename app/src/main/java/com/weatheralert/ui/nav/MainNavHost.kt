package com.weatheralert.ui.nav
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.weatheralert.FavoritePage
import com.weatheralert.HomePage
import com.weatheralert.MainViewModel
import com.weatheralert.MapPage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavHost(navController: NavHostController,viewModel: MainViewModel) {
    NavHost(navController, startDestination = Route.Home.route) {
        composable(Route.Home.route) { HomePage(viewModel = viewModel) }
        composable(Route.List.route) { FavoritePage(viewModel = viewModel) }
        composable(Route.Map.route) { MapPage(viewModel = viewModel) }

    }
}