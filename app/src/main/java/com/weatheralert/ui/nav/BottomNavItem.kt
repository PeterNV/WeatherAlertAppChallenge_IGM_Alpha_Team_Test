package com.weatheralert.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
@Serializable
sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Search : Route("list")
    data object Map : Route("map")

    var value: String
        get() = TODO("Not yet implemented")
        set(value) {}

  fun component1(): String {
        TODO("Not yet implemented")
    }

   fun component2(): (String) -> Unit {
        TODO("Not yet implemented")
    }
}
sealed class BottomNavItem(
    var title: String,
    var icon: ImageVector,
    var route: Route)
{
    data object HomeButton :
        BottomNavItem("Local", Icons.Default.Home, Route.Home)
    data object ListButton :
        BottomNavItem("Search", Icons.Default.Search, Route.Search)
    data object MapButton  :
        BottomNavItem("Map", Icons.Default.LocationOn, Route.Map)
}