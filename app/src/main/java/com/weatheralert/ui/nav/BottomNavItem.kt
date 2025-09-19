package com.weatheralert.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
@Serializable
sealed class Route(val route: String) {
    data object Home : Route("home")
    data object List : Route("list")
    data object Map : Route("map")

    var value: String
        get() = TODO("Not yet implemented")
        set(value) {}
}

sealed class BottomNavItem(
    var title: String,
    var icon: ImageVector,
    var route: Route)
{
    data object HomeButton :
        BottomNavItem("Local", Icons.Default.Home, Route.Home)
    data object ListButton :
        BottomNavItem("Favorite", Icons.Default.Favorite, Route.List)
    data object MapButton  :
        BottomNavItem("Map", Icons.Default.LocationOn, Route.Map)
}