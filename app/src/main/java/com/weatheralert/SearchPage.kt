package com.weatheralert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatheralert.model.City
import com.weatheralert.ui.theme.Black
import com.weatheralert.ui.theme.White
import android.app.Application
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.weatheralert.repo.Repository
import kotlinx.coroutines.launch

class FavoritosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)

    val favoriteCities = repository.favoriteCities

    fun addFavoriteCity(city: City) {
        viewModelScope.launch {
            repository.addFavoriteCity(city)
        }
    }

    fun addFavoriteCityByName(name: String) {
        viewModelScope.launch {
            repository.addFavoriteCity(City(name = name))
        }
    }

    suspend fun isCityFavorite(cityName: String): Boolean {
        return repository.isCityFavorite(cityName)
    }

    fun removeFavoriteCity(city: City) {
        viewModelScope.launch {
            repository.removeFavoriteCity(city)
        }
    }
}
//@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritePage(modifier: Modifier = Modifier, viewModel: FavoritosViewModel) {
    val favoriteCities by viewModel.favoriteCities.collectAsState(initial = emptyList())

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.background(color = White).fillMaxSize()
    ) {
        if (favoriteCities.isEmpty()) {
            Text("No favorites", color = Black, fontSize = 18.sp)
        } else {
            LazyColumn (horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center){
                items(favoriteCities) { city ->

                    Text(
                        text = "${city.name}",
                        color = Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp).shadow(5.dp, RoundedCornerShape(25.dp)).background(color = White, shape = RoundedCornerShape(25.dp)).height(55.dp).width(350.dp).border(3.dp, color = White, shape = RoundedCornerShape(25.dp))
                    )
                }
            }
        }
    }
}