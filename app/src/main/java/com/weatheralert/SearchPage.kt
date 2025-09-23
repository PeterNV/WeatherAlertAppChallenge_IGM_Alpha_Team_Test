package com.weatheralert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatheralert.model.City
import com.weatheralert.ui.theme.Black
import com.weatheralert.ui.theme.White
import android.app.Application
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.simulateHotReload
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.weatheralert.api.WeatherApiResponse
import com.weatheralert.api.WeatherServiceAPI
import com.weatheralert.repo.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// Data class para armazenar dados meteorológicos de cada cidade
data class CityWeatherData(
    val cityName: String,
    val temperature: String = "--",
    val humidity: String = "--",
    val rain: String = "--",
    val wind: String = "--",
    val uv: String = "--",
    val weatherIcon: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class FavoritosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)
    val favoriteCities = repository.favoriteCities

    // Mapa para armazenar dados meteorológicos de cada cidade
    private val _citiesWeatherData = MutableStateFlow<Map<String, CityWeatherData>>(emptyMap())
    val citiesWeatherData = _citiesWeatherData.asStateFlow()

    fun addFavoriteCity(city: City) {
        viewModelScope.launch {
            repository.addFavoriteCity(city)
            // Carrega dados meteorológicos quando uma cidade é adicionada
            loadWeatherData(city.name)
        }
    }

    fun addFavoriteCityByName(name: String) {
        viewModelScope.launch {
            repository.addFavoriteCity(City(name = name))
            loadWeatherData(name)
        }
    }


    fun removeFavoriteCity(city: City) {
        viewModelScope.launch {
            repository.removeFavoriteCity(city)
            // Remove dados meteorológicos quando a cidade é removida
            removeWeatherData(city.name)
            simulateHotReload(
                context = getApplication()
            )
        }
    }

    fun loadWeatherDataForAllCities() {
        viewModelScope.launch {
            // Coletamos o valor atual do favoriteCities
            favoriteCities.collect { cities ->
                cities.forEach { city ->
                    loadWeatherData(city.name)
                }
            }
        }
    }

    private fun loadWeatherData(cityName: String) {
        // Atualiza estado para loading
        updateCityWeatherData(cityName, CityWeatherData(cityName, isLoading = true))

        val call = weatherService.getWeatherForecast(cityName)
        call.enqueue(object : Callback<WeatherApiResponse> {
            override fun onResponse(
                call: Call<WeatherApiResponse>,
                response: Response<WeatherApiResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    val weatherData = CityWeatherData(
                        cityName = cityName,
                        temperature = data?.current?.temp_c?.toString() ?: "--",
                        humidity = data?.current?.humidity?.toString() ?: "--",
                        rain = data?.current?.precip_mm?.toString() ?: "--",
                        wind = data?.current?.wind_kph?.toString() ?: "--",
                        uv = data?.current?.uv?.toString() ?: "--",
                        weatherIcon = "https:${data?.current?.condition?.icon ?: ""}",
                        isLoading = false
                    )
                    updateCityWeatherData(cityName, weatherData)
                } else {
                    updateCityWeatherData(
                        cityName,
                        CityWeatherData(
                            cityName,
                            error = "Erro: ${response.code()}",
                            isLoading = false
                        )
                    )
                    Log.e("Weather", "Erro na API: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WeatherApiResponse>, t: Throwable) {
                updateCityWeatherData(
                    cityName,
                    CityWeatherData(
                        cityName,
                        error = "Falha: ${t.message}",
                        isLoading = false
                    )
                )
                Log.e("Weather", "Falha na requisição: ${t.message}")
            }
        })
    }

    private fun removeWeatherData(cityName: String) {
        val currentData = _citiesWeatherData.value.toMutableMap()
        currentData.remove(cityName)
        _citiesWeatherData.value = currentData
    }

    private fun updateCityWeatherData(cityName: String, weatherData: CityWeatherData) {
        val currentData = _citiesWeatherData.value.toMutableMap()
        currentData[cityName] = weatherData
        _citiesWeatherData.value = currentData
    }

    private val weatherService: WeatherServiceAPI by lazy {
        Retrofit.Builder()
            .baseUrl(WeatherServiceAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherServiceAPI::class.java)
    }
}


@Composable
fun FavoritePage(modifier: Modifier = Modifier, viewModel: FavoritosViewModel) {
    val favoriteCities by viewModel.favoriteCities.collectAsState(initial = emptyList())
    val citiesWeatherData by viewModel.citiesWeatherData.collectAsState()


    var historicalWeatherData by remember { mutableStateOf("") }



    val context = LocalContext.current

    // Carrega dados meteorológicos quando a tela é exibida
    LaunchedEffect(Unit) {
        viewModel.loadWeatherDataForAllCities()

    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.background(color = White).fillMaxSize()
    ) {

        if (favoriteCities.isEmpty()) {
            Text("No favorites", color = Black, fontSize = 18.sp)
        } else {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                items(favoriteCities) { city ->
                    val weatherData = citiesWeatherData[city.name] ?: CityWeatherData(city.name)
                    if (city.name.isNotBlank()) {
                        fetchHistoricalWeatherData(city.name, context) { data ->
                            historicalWeatherData = data
                        }
                    }


                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = modifier
                            .shadow(5.dp, RoundedCornerShape(25.dp))
                            .background(color = White, shape = RoundedCornerShape(25.dp))
                            .height(420.dp)
                            .width(350.dp)
                            .border(3.dp, color = White, shape = RoundedCornerShape(25.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (weatherData.isLoading) {
                            CircularProgressIndicator()
                            Text("Loading weather data...", fontSize = 12.sp, color = Black)
                        } else if (weatherData.error != null) {
                            Text(city.name, fontWeight = FontWeight.Bold, color = Black)
                            Text("Error loading data", fontSize = 12.sp, color = Black)
                        } else {
                            Row{
                                Text(
                                    text = city.name,
                                    fontWeight = FontWeight.Bold,
                                    color = Black,
                                    fontSize = 18.sp
                                )
                                AsyncImage(
                                    model = weatherData.weatherIcon,
                                    contentDescription = "Weather icon",
                                    modifier = Modifier
                                        .size(35.dp)
                                        .offset(0.dp,(-5).dp)

                                )
                                Button(
                                    modifier = modifier.height(35.dp).offset(10.dp,(-8).dp),
                                    onClick = {
                                        viewModel.removeFavoriteCity(city)
                                    }
                                ){
                                    Icon(
                                        imageVector =  Icons.Filled.Delete,
                                        contentDescription = "Remove icon"
                                    )
                                }
                            }

                            Text(
                                text = "Temperature: ${weatherData.temperature}°C",
                                color = Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Humidity: ${weatherData.humidity}%",
                                color = Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Rain: ${weatherData.rain}mm",
                                color = Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Wind: ${weatherData.wind} km/h",
                                color = Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Uv: ${weatherData.uv} ",
                                color = Black,
                                fontSize = 14.sp
                            )


                        }

                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}