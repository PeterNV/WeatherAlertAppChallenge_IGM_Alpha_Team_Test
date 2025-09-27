package com.weatheralert


import android.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize

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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.simulateHotReload
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import coil.compose.AsyncImage
import com.google.ai.client.generativeai.GenerativeModel
import com.weatheralert.api.WeatherApiResponse
import com.weatheralert.api.WeatherServiceAPI
import com.weatheralert.repo.Repository
import com.weatheralert.ui.theme.GrayD
import com.weatheralert.ui.theme.GreenL
import com.weatheralert.ui.theme.Red
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate

// Data class para armazenar dados meteorológicos de cada cidade
data class CityWeatherData(
    val cityName: String,
    val temperature: String = "--",
    val humidity: String = "--",
    val rain: String = "--",
    val wind: String = "--",

    val weatherIcon: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritePage(modifier: Modifier = Modifier, viewModel: FavoritesViewModel) {
    val favoriteCities by viewModel.favoriteCities.collectAsState(initial = emptyList())
    val citiesWeatherData by viewModel.citiesWeatherData.collectAsState()
    val today = LocalDate.now()
    val optionsDay: List<String> = (1..31).map { it.toString() }
    val optionsMonth: List<String> = (today.monthValue..12).map { it.toString() }
    var expandedDay by remember { mutableStateOf(false) }
    var expandedCity by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }
    var historicalWeatherData by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf("Day") }
    var selectedCity by remember { mutableStateOf("City") }
    var selectedMonth by remember { mutableStateOf("Month") }
    var isLoadingDateChoose by remember { mutableStateOf(false) }
    var geminiResponseDateChoose by remember { mutableStateOf("") }
    val modelIa = GenerativeModel(modelName = "gemini-2.0-flash", apiKey = BuildConfig.GEMINI_API_KEY)
    val context = LocalContext.current
    var closeDateChoose by remember { mutableStateOf(false) }
    var showDateChoose by remember { mutableStateOf(false) }
    var allButsDisabled by remember { mutableStateOf(false) }
    var showFirstRecDateChoose by remember { mutableStateOf(true) }
    var isLoadingToday by remember { mutableStateOf(false) }
    var geminiResponseToday by remember { mutableStateOf("") }
    var closeRecToday by remember { mutableStateOf(false) }
    var showRecToday by remember { mutableStateOf(false) }
    var showFirstRecToday by remember { mutableStateOf(true) }
    var closeRec5Days by remember { mutableStateOf(false) }
    var showRec5Days by remember { mutableStateOf(false) }
    var showFirstForecast by remember { mutableStateOf(false) }
    var isLoading5Days by remember { mutableStateOf(false) }
    var geminiResponse5Days by remember { mutableStateOf("") }
    var showFirstRec5Days by remember { mutableStateOf(true) }
    // Carrega dados meteorológicos quando a tela é exibida
    LaunchedEffect(Unit) {
        viewModel.loadWeatherDataForAllCities()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = modifier.fillMaxSize().background(color = White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val optionsFavorites: List<String> = favoriteCities.map { it.name }
            ExposedDropdownMenuBox(
                expanded = expandedCity,
                onExpandedChange = { expandedCity = !expandedCity },
                modifier = modifier.background(color = Color.Transparent)
            ) {
                TextField(
                    value = selectedCity,
                    onValueChange = {showFirstForecast = true
                        Log.d(String.toString(),showFirstForecast.toString())},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCity) },
                    modifier = Modifier.menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedCity,
                    onDismissRequest = { expandedCity = false },
                    containerColor = White,
                    modifier = modifier.background(color = Color.Transparent)
                ) {
                    optionsFavorites.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                selectedCity = selectionOption
                                expandedCity = false
                                showFirstForecast = true
                                allButsDisabled = false
                                Log.d(String.toString(),showFirstForecast.toString())
                            },

                            )
                    }
                }
            }
            val weatherData = citiesWeatherData[selectedCity] ?: CityWeatherData(selectedCity)
            if(selectedCity != "City"){

                if (weatherData.isLoading) {
                    CircularProgressIndicator()
                    Text("Loading weather data...", fontSize = 12.sp, color = Black)
                } else if (weatherData.error != null) {
                    Text(selectedCity, fontWeight = FontWeight.Bold, color = Black)
                    Text("Error loading data", fontSize = 12.sp, color = Black)
                } else {

                    Row{
                        Text(
                            text = selectedCity,
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
                                val cityToRemove = favoriteCities.find { it.name == selectedCity }
                                cityToRemove?.let {
                                    viewModel.removeFavoriteCity(it)
                                    // Resetar a seleção após remover
                                    selectedCity = "City"
                                }

                            },
                            colors = ButtonColors(
                                containerColor = Red,
                                contentColor = White,
                                disabledContainerColor = GrayD,
                                disabledContentColor = White,
                            ),
                            enabled = allButsDisabled
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
                    var isLoading by remember { mutableStateOf(false) }
                    var geminiResponse by remember { mutableStateOf("") }
                    val fetchHistoricalData: () -> Unit = {
                        if (selectedCity.isNotBlank()) {
                            isLoading = true
                            geminiResponse = ""

                            fetchHistoricalWeatherData(selectedCity, context) { data ->
                                historicalWeatherData = data

                                // Agora gerar o conteúdo com Gemini
                                val weatherPrompt1 =
                                    "Considerando que estou em ${selectedCity}, o ano é ${today.year}, considere o fuso horário desse lugar.  " +
                                            "os dados de hoje são ${weatherData.temperature}ºC, ${weatherData.humidity}%, " +
                                            "${weatherData.rain}mm/h e ${weatherData.wind}km/h"

                                val weatherPrompt2 =
                                    "Dados históricos dos últimos 7 dias: $historicalWeatherData. " +
                                            "Gere apenas 10 valores, 5 da temperatura em ºC e 5 das condições possíveis para os próximos 5 dias, e não inclua mais textos além disso e lembre-se de incluir essas imagens nas previsões https://www.weatherapi.com/docs/weather_conditions.json e as imagens tem que ficar do lado de ºC. Exemplo do que você deve gerar: " +

                                            "27,5ºC 28,1ºC 25,1ºC 26,1ºC 23,1ºC (Esses valores são apenas exemplos e não devem ser gerados)"

                                // Gerar previsão com Gemini em uma corrotina separada
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val fullPrompt = weatherPrompt1 + weatherPrompt2
                                        val response = modelIa.generateContent(fullPrompt)
                                        geminiResponse = response.text ?: "Não foi possível gerar previsão"
                                    } catch (e: Exception) {
                                        geminiResponse = "Erro ao gerar previsão: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        } else {
                            geminiResponse = "Por favor, selecione uma cidade primeiro"
                        }
                    }
                    val fetchHistoricalToday: () -> Unit = {
                        if (selectedCity.isNotBlank()) {
                            isLoadingToday = true
                            geminiResponseToday = ""

                            fetchHistoricalWeatherData(selectedCity, context) { data ->
                                historicalWeatherData = data

                                // Agora gerar o conteúdo com Gemini
                                val weatherPrompt1 =
                                    "Considerando que estou em ${selectedCity}, considere o fuso horário dessa cidade, e não esqueça que estamos em " + "${today.year}, " +
                                            "os dados de agora são ${weatherData.temperature}ºC, ${weatherData.humidity}%, " +
                                            "${weatherData.rain}mm/h e ${weatherData.wind}km/h "

                                val weatherPrompt2 =
                                    "Quais são as recomendações, que tenham haver com trabalho, estudo, lazer, sair de casa, saúde, etc. Lembre-se também de avisar se é seguro sair de casa ou não."

                                // Gerar previsão com Gemini em uma corrotina separada
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val fullPrompt = weatherPrompt1 + weatherPrompt2
                                        val response = modelIa.generateContent(fullPrompt)
                                        geminiResponseToday =
                                            response.text ?: "Não foi possível gerar recomendações"
                                    } catch (e: Exception) {
                                        geminiResponseToday = "Erro ao gerar previsão: ${e.message}"
                                    } finally {
                                        isLoadingToday = false
                                    }
                                }
                            }
                        } else {
                            geminiResponseToday = "Por favor, selecione uma cidade primeiro"
                        }
                    }
                    val fetchHistoricalDateChoose: () -> Unit = {
                        if (selectedCity.isNotBlank()) {
                            isLoadingDateChoose = true
                            geminiResponseDateChoose = ""

                            fetchHistoricalWeatherData(selectedCity, context) { data ->
                                historicalWeatherData = data

                                // Agora gerar o conteúdo com Gemini
                                val weatherPrompt1 =
                                    "Considerando que estou em ${selectedCity}, qual é a previsão para "+selectedDay+"/"+selectedMonth+"/"+today.year+"?"+" Considere esses dados de 9 dias anteriores a hoje ($today): $historicalWeatherData"+
                                            "os dados de agora são ${weatherData.temperature}ºC, ${weatherData.humidity}%, " +
                                            "${weatherData.rain}mm/h e ${weatherData.wind}km/h "

                                val weatherPrompt2 =
                                    "Quais são as recomendações para essa data, que tenham haver com trabalho, estudo, lazer,  sair de casa, etc.? Lembre-se também de avisar se é seguro sair de casa ou não. E caso a data seja invalida reponda apenas DATA INVALIDA, e lembre-se de que hoje é "+
                                            "$today"+"."

                                // Gerar previsão com Gemini em uma corrotina separada
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val fullPrompt = weatherPrompt1 + weatherPrompt2
                                        val response = modelIa.generateContent(fullPrompt)
                                        geminiResponseDateChoose =
                                            response.text ?: "Não foi possível gerar recomendações"
                                    } catch (e: Exception) {
                                        geminiResponseDateChoose = "Erro ao gerar previsão: ${e.message}"
                                    } finally {
                                        isLoadingDateChoose = false
                                    }
                                }
                            }
                        } else {
                            geminiResponseDateChoose = "Por favor, selecione uma cidade primeiro"
                        }
                    }
                    val fetchHistorical5Days: () -> Unit = {
                        if (selectedCity.isNotBlank()) {
                            isLoading5Days = true
                            geminiResponse5Days = ""

                            fetchHistoricalWeatherData(selectedCity, context) { data ->
                                historicalWeatherData = data

                                // Agora gerar o conteúdo com Gemini
                                val weatherPrompt1 =
                                    "Considerando que estou em ${selectedCity}, e essas são as previsões para os próximos 5 dias "+geminiResponse

                                val weatherPrompt2 =
                                    "Quais são as recomendações para os próximos 5 dias, que tenham haver com trabalho, estudo, lazer,  sair de casa, etc.? Lembre-se também de avisar se é seguro sair de casa ou não."

                                // Gerar previsão com Gemini em uma corrotina separada
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val fullPrompt = weatherPrompt1 + weatherPrompt2
                                        val response = modelIa.generateContent(fullPrompt)
                                        geminiResponse5Days =
                                            response.text ?: "Não foi possível gerar recomendações"
                                    } catch (e: Exception) {
                                        geminiResponse5Days = "Erro ao gerar previsão: ${e.message}"
                                    } finally {
                                        isLoading5Days = false
                                    }
                                }
                            }
                        } else {
                            geminiResponseToday = "Por favor, selecione uma cidade primeiro"
                        }
                    }
                    val extraDays = (1..5).map { today.plusDays(it.toLong()).dayOfMonth.toString() + "/" + today.plusDays(it.toLong()).monthValue.toString() }
                    if(showFirstForecast){
                        fetchHistoricalData()
                        showFirstForecast = false
                    }
                    if (showRecToday && !closeRecToday) {
                        if (showFirstRecToday) {
                            fetchHistoricalToday()
                            showFirstRecToday = false
                        }

                        if (isLoadingToday) {

                            CircularProgressIndicator()
                            allButsDisabled = false
                        } else {

                            if (geminiResponseToday.isNotEmpty()) {
                                val builder = AlertDialog.Builder(context)

                                builder.setNegativeButton("X") { dialog,_ ->

                                    allButsDisabled = true
                                    showRecToday =  false
                                    closeRecToday = true
                                    showFirstRecToday = true

                                    dialog.dismiss()
                                }
                                builder.setTitle("Recommendations (Today)")
                                builder.setMessage(geminiResponseToday.replace("*",""))

                                val dialog = builder.create()
                                dialog.show()
                            }

                        }
                    }
                    if (showDateChoose && !closeDateChoose) {
                        if (showFirstRecDateChoose) {
                            fetchHistoricalDateChoose()
                            showFirstRecDateChoose = false
                        }

                        if (isLoadingDateChoose) {

                            CircularProgressIndicator()
                            allButsDisabled = false
                        } else {

                            if (geminiResponseDateChoose.isNotEmpty()) {
                                val builder = AlertDialog.Builder(context)

                                builder.setNegativeButton("X") { dialog,_ ->
                                    allButsDisabled = true
                                    showDateChoose  = false
                                    closeDateChoose = true
                                    showFirstRecDateChoose = true
                                    selectedDay = "Day"
                                    selectedMonth = "Month"
                                    dialog.dismiss()
                                }
                                builder.setTitle("Recommendations (date choose)")
                                builder.setMessage(geminiResponseDateChoose.replace("*",""))

                                val dialog = builder.create()
                                dialog.show()
                            }

                        }
                    }

                    if (showRec5Days && !closeRec5Days) {
                        if (showFirstRec5Days) {
                            fetchHistorical5Days()
                            showFirstRec5Days = false
                        }

                        if (isLoading5Days) {

                            CircularProgressIndicator()
                            allButsDisabled = false
                        } else {

                            if (geminiResponse5Days.isNotEmpty()) {
                                val builder = AlertDialog.Builder(context)

                                builder.setNegativeButton("X") { dialog,_ ->

                                    showRec5Days = false
                                    closeRec5Days = true
                                    dialog.dismiss()
                                }
                                builder.setTitle("Recommendations (next 5 days)")
                                builder.setMessage(geminiResponse5Days.replace("*",""))

                                val dialog = builder.create()
                                dialog.show()
                            }

                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {

                        ExposedDropdownMenuBox(
                            expanded = expandedDay,
                            onExpandedChange = { expandedDay = !expandedDay },
                            modifier = modifier.width(105.dp).offset((-40).dp, (-20).dp)
                                .background(color = Color.Transparent)
                        ) {
                            TextField(
                                value = selectedDay,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDay) },
                                modifier = Modifier.menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.textFieldColors(
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDay,
                                onDismissRequest = { expandedDay = false },
                                containerColor = White,
                                modifier = modifier.background(color = Color.Transparent)
                            ) {

                                optionsDay.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            selectedDay = selectionOption
                                            expandedDay = false
                                        },

                                        )
                                }
                            }
                        }
                        ExposedDropdownMenuBox(
                            expanded = expandedMonth,
                            onExpandedChange = { expandedMonth = !expandedMonth },
                            modifier = modifier.width(125.dp).offset(45.dp, (-20).dp),

                            ) {
                            TextField(
                                value = selectedMonth,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                                modifier = Modifier.menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.textFieldColors(
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedMonth,
                                onDismissRequest = { expandedMonth = false },
                                modifier = modifier.background(color = White),

                                ) {
                                optionsMonth.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            selectedMonth = selectionOption
                                            expandedMonth = false
                                        },

                                        )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        colors = ButtonColors(
                            containerColor = GreenL,
                            contentColor = White,
                            disabledContainerColor = GrayD,
                            disabledContentColor = White,
                        ),
                        modifier = modifier
                            .height(50.dp)
                            .border(3.dp, Color.Transparent, RoundedCornerShape(25.dp)),
                        onClick = {
                            showDateChoose  = true
                            closeDateChoose = false
                            allButsDisabled = false
                            showFirstRecDateChoose = true
                        },
                        enabled = allButsDisabled
                    ) {
                        Text("Forecast date", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        colors = ButtonColors(
                            containerColor = GreenL,
                            contentColor = White,
                            disabledContainerColor = GrayD,
                            disabledContentColor = White,
                        ),
                        modifier = modifier
                            .height(50.dp)
                            .border(3.dp, Color.Transparent, RoundedCornerShape(25.dp)),
                        onClick = {
                            showRecToday = true
                            closeRecToday = false
                            allButsDisabled = false
                            showFirstRecDateChoose = true
                        },
                        enabled = allButsDisabled
                    ) {
                        Text("Recommendations (today)", fontSize = 16.sp)
                    }
                    Button(
                        colors = ButtonColors(
                            containerColor = GreenL,
                            contentColor = White,
                            disabledContainerColor = GrayD,
                            disabledContentColor = White,
                        ),
                        modifier = modifier
                            .height(50.dp)
                            .offset((-5).dp, (25).dp)
                            .border(3.dp, Color.Transparent, RoundedCornerShape(25.dp)),
                        onClick = {
                            showRec5Days = true
                            closeRec5Days = false
                            showFirstRec5Days = true
                        },
                        enabled = allButsDisabled
                    ) {
                        Text("Recommendations (next 5 days)", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier

                            .offset(0.dp, 45.dp)
                            .background(
                                brush = Brush.verticalGradient( // Or Brush.horizontalGradient, Brush.linearGradient
                                    colors = listOf(
                                        Color(0xFF64B5F6), // Start color (light blue)
                                        Color(0xFF0D47A1)  // End color (dark blue)
                                    )
                                ), shape = RoundedCornerShape(15.dp)
                            )
                            .border(3.dp, color = Color.Transparent, shape = RoundedCornerShape(15.dp))
                            .height(125.dp)
                            .width(175.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (geminiResponse.isNotEmpty()) {
                            val BrokenLines = countBrokenLines(geminiResponse)

                            Row {
                                if (geminiResponse.contains("<img")
                                    || geminiResponse.contains("https")
                                    || geminiResponse.contains("Ensolarado")
                                    || geminiResponse.contains("Nublado")
                                    || geminiResponse.contains("Sunny")
                                    || geminiResponse.contains("Cloudly")
                                    || geminiResponse.contains("Sol")
                                    || geminiResponse.contains("png")
                                    || geminiResponse.contains(".png")
                                    || BrokenLines < 5
                                    || BrokenLines > 5
                                ) {
                                    showFirstForecast = true
                                    allButsDisabled = false
                                } else {
                                    allButsDisabled = true
                                    Text(
                                        text = geminiResponse.replace("*",""),
                                        fontSize = 16.sp,
                                        color = White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 5
                                    )
                                }

                                Text(
                                    text = extraDays.toString().replace("[", " ").replace("]", "")
                                        .replace(",", "\n"),
                                    fontSize = 16.sp,
                                    color = White,
                                    fontWeight = FontWeight.Bold

                                )
                            }
                        }
                    }
                }

            }else{
                allButsDisabled = false
            }
        }

    }
}