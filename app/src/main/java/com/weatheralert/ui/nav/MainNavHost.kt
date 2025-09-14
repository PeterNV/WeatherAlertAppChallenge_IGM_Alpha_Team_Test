package com.weatheralert.ui.nav
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.weatheralert.HomePage
import com.weatheralert.MainViewModel
import com.weatheralert.MapPage
import com.weatheralert.SearchPage

@Composable
fun MainNavHost(navController: NavHostController,viewModel: MainViewModel) {
    NavHost(navController, startDestination = Route.Home.route) {
        composable(Route.Home.route) { HomePage(viewModel = viewModel) }
        composable(Route.Search.route) { SearchPage(viewModel = viewModel) }
        composable(Route.Map.route) { MapPage(viewModel = viewModel) }


    }
    }