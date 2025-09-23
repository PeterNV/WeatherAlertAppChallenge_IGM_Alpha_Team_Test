package com.weatheralert

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon

import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally


import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage

import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import java.time.LocalDate
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.UrlTileProvider
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.weatheralert.model.City

import com.weatheralert.ui.theme.GrayD
import com.weatheralert.ui.theme.GrayL
import com.weatheralert.ui.theme.GreenL
import com.weatheralert.ui.theme.Red
import com.weatheralert.ui.theme.White
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL


@ExperimentalMaterial3Api
@Composable
fun MapPage(modifier: Modifier = Modifier, viewModel: MainViewModel,favoritosViewModel: FavoritosViewModel) {
    val context = LocalContext.current
    val currentTime = LocalDate.now()
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val cameraPositionState = rememberCameraPositionState()
    val modelIa = GenerativeModel(modelName = "gemini-2.0-flash", apiKey = BuildConfig.GEMINI_API_KEY)
    var cityLong by rememberSaveable { mutableStateOf<Double?>(null) }
    var cityLati by rememberSaveable { mutableStateOf<Double?>(null) }
    var cityState by rememberSaveable { mutableStateOf<Boolean?>(false) }
    var expandedDay by remember { mutableStateOf(false) }
    var expandedMap by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }
    var cityShow by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf("Day") }
    var selectedMonth by remember { mutableStateOf("Month") }
    var selectedMap by remember { mutableStateOf("Select map") }
    var CityName by remember { mutableStateOf("") }
    var CitySearch by remember { mutableStateOf(true) }
    val optionsDay: List<String> = (1..31).map { it.toString() }
    val optionsMonth: List<String> = (1..12).map { it.toString() }
    val optionsMap = listOf("Normal","MODIS Terra True Color")
    var map1Chooser by remember { mutableStateOf(true) }  // ← CORRETO
    var map2Chooser by remember { mutableStateOf(false) } // ← CORRETO
    var historicalWeatherData by remember { mutableStateOf("") }
    var showFirstForecast by remember { mutableStateOf(false) }
    var showFirstForecastMap by remember { mutableStateOf(false) }
    var geminiResponse by remember { mutableStateOf("") }
    val today = LocalDate.now()
    var isFavorite by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    // Efeito para buscar dados históricos quando a cidade mudar
    LaunchedEffect(viewModel.cidadeSearch.value) {
        if (viewModel.cidadeSearch.value.isNotBlank()) {
            fetchHistoricalWeatherData(viewModel.cidadeSearch.value, context) { data ->
                historicalWeatherData = data
            }
        }
    }


    val fetchHistoricalDataSearch: () -> Unit = {
        if (viewModel.cidadeSearch.value.isNotBlank()) {
            isLoading = true
            geminiResponse = ""

            fetchHistoricalWeatherData(viewModel.cidadeSearch.value, context) { data ->
                historicalWeatherData = data

                // Agora gerar o conteúdo com Gemini
                val weatherPrompt1 = "Considerando que estou em ${viewModel.cidadeSearch.value}, hoje é $today, " +
                        "os dados de hoje são ${viewModel.temperaturaSearch.value}ºC, ${viewModel.umidadeSearch.value}%, " +
                        "${viewModel.chuvaSearch.value}mm/h, ${viewModel.ventoSearch.value}km/h e índice de uv: ${viewModel.uvSearch.value}"

                val weatherPrompt2 = "Dados históricos dos últimos 7 dias: $historicalWeatherData. " +
                        "Gere apenas 10 valores, 5 da temperatura em ºC e 5 das condições possíveis para os próximos 5 dias, e não inclua mais textos além disso e lembre-se de incluir essas imagens nas previsões https://www.weatherapi.com/docs/weather_conditions.json e as imagens tem que ficar do lado de ºC. Exemplo do que você deve gerar: " +

                        "27.5ºC 28.1ºC 25.1ºC 26.1ºC 23.1ºC (Esses valores são apenas exemplos e não devem ser gerados)"

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
    val fetchHistoricalDataMap: () -> Unit = {
        if (viewModel.cidadeMap.value.isNotBlank()) {
            isLoading = true
            geminiResponse = ""

            fetchHistoricalWeatherData(viewModel.cidadeMap.value, context) { data ->
                historicalWeatherData = data

                // Agora gerar o conteúdo com Gemini
                val weatherPrompt1 = "Considerando que estou em ${viewModel.cidadeMap.value}, hoje é $today, " +
                        "os dados de hoje são ${viewModel.temperaturaMap.value}ºC, ${viewModel.umidadeMap.value}%, " +
                        "${viewModel.chuvaMap.value}mm/h, ${viewModel.ventoMap.value}km/h e índice de uv: ${viewModel.uvMap.value}"

                val weatherPrompt2 = "Dados históricos dos últimos 7 dias: $historicalWeatherData. " +
                        "Gere apenas 10 valores, 5 da temperatura em ºC e 5 das condições possíveis para os próximos 5 dias, e não inclua mais textos além disso e lembre-se de incluir essas imagens nas previsões https://www.weatherapi.com/docs/weather_conditions.json e as imagens tem que ficar do lado de ºC. Exemplo do que você deve gerar: " +

                        "27.5ºC 28.1ºC 25.1ºC 26.1ºC 23.1ºC (Esses valores são apenas exemplos e não devem ser gerados)"

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
    Box(modifier = modifier.fillMaxSize()) {

        // --- Google Map ---
        if(map1Chooser == true  ){
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(zoomControlsEnabled = true),
                cameraPositionState = cameraPositionState,
                onMapClick = { location ->
                    cityLong = location.longitude
                    cityLati = location.latitude
                    viewModel.getCityMap(location.latitude, location.longitude)
                    Log.d("MapClick", "Lat: $cityLati, Lon: $cityLong")
                    cityState = true;
                    showFirstForecastMap = true;
                }
            ) {
                // Você pode adicionar Marker aqui se quiser
                cityLati?.let { lat ->
                    cityLong?.let { lon ->
                        Marker(state = MarkerState(LatLng(lat, lon)), title = "Selected Location")

                    }
                }
            }
        }

        if(map2Chooser == true){
            AndroidView(
                factory = { context: Context ->
                    val mapView = MapView(context)
                    mapView.onCreate(Bundle())
                    mapView.getMapAsync { googleMap ->
                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(LatLng(-10.0, -55.0), 4f)
                        )

                        val gibsTileProvider = object : UrlTileProvider(256, 256) {
                            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL {
                                val layer = "VIIRS_SNPP_CorrectedReflectance_TrueColor"
                                val url =
                                    "https://gibs.earthdata.nasa.gov/wmts/epsg3857/best/$layer/default/$currentTime/GoogleMapsCompatible_Level9/$zoom/$y/$x.jpg"
                                return URL(url)
                            }
                        }

                        googleMap.addTileOverlay(TileOverlayOptions().tileProvider(gibsTileProvider))
                    }
                    mapView
                },
                modifier = modifier.fillMaxSize()
            )
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)


                .height(100.dp).width(215.dp)
        ){
            ExposedDropdownMenuBox(
                expanded = expandedMap,
                onExpandedChange = { expandedMap = !expandedMap },
                modifier = modifier.width(175.dp).background(color = Color.Transparent)
            ) {
                TextField(
                    value = selectedMap,
                    onValueChange = {},
                    readOnly = true,
                    shape = RoundedCornerShape(25.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMap) },
                    modifier = Modifier.border(3.dp, color = GrayL, shape = RoundedCornerShape(25.dp)).menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = White,
                        unfocusedContainerColor = White,
                        focusedContainerColor = White
                    )

                )
                ExposedDropdownMenu(
                    expanded = expandedMap,
                    onDismissRequest = { expandedMap = false },
                    containerColor = White,
                    modifier = modifier.background(color = Color.Transparent).menuAnchor()
                ) {

                    optionsMap.forEach { selectionOption ->
                        DropdownMenuItem(

                            text = { Text(selectionOption) },
                            onClick = {
                                selectedMap = selectionOption
                                expandedMap = false
                                Log.d(toString(),selectedMap)
                                if(selectionOption == "Select map" || selectionOption == "Normal"){
                                    map1Chooser = true
                                    map2Chooser = false
                                    Log.d(toString(), map1Chooser.toString())
                                    Log.d(toString(), map2Chooser.toString())
                                }

                                if(selectionOption == "MODIS Terra True Color"){
                                    map1Chooser = false
                                    map2Chooser = true
                                    Log.d(toString(), map1Chooser.toString())
                                    Log.d(toString(), map2Chooser.toString())
                                }
                            },

                            )
                    }
                }
            }
        }
        Column (horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier.offset(5.dp,650.dp).background(color = White, shape = RoundedCornerShape(25.dp))
                .height(65.dp)
                .width(325.dp)
                .border(3.dp, color = White, shape = RoundedCornerShape(25.dp))) {
            Row{
                OutlinedTextField(
                    value = CityName,
                    onValueChange = {CityName = it},
                    modifier = modifier.border(3.dp, color = GrayD, shape = RoundedCornerShape(25.dp)).height(55.dp).width(215.dp),
                    placeholder = { Text("City name", color = GrayL) },
                    shape = RoundedCornerShape(25.dp),
                    enabled = CitySearch
                )
                Button(
                    onClick = {
                        cityShow = true
                        viewModel.getCity(CityName)
                        CitySearch = false
                        showFirstForecast = true
                    },
                    modifier = modifier.offset(10.dp).height(55.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonColors(
                        containerColor = GreenL,
                        contentColor = White,
                        disabledContentColor = White,
                        disabledContainerColor = GreenL
                    )
                ) { Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search icon"
                ) }
            }

        }

        // --- Box de informações meteorológicas ---
        if ((cityLati != null && cityLong != null && cityState == true) ) {
            if(showFirstForecastMap == true){
                fetchHistoricalDataMap()
                showFirstForecastMap = false
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(brush = Brush.verticalGradient( // Or Brush.horizontalGradient, Brush.linearGradient
                        colors = listOf(
                            White, // Start color (light blue)
                            Color(0xFF0D47A1)  // End color (dark blue)
                        )
                    ), shape = RoundedCornerShape(15.dp))
                    .border(3.dp, color = Color.Transparent, RoundedCornerShape(12.dp))
                    .padding(48.dp).height(600.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Row{
                    Button(onClick = {
                        cityState = false;
                        showFirstForecastMap = false;
                    },
                        modifier = modifier.height(50.dp).offset(0.dp,(-10).dp),
                        colors = ButtonColors(
                            containerColor = Red,
                            contentColor = White,
                            disabledContainerColor = Red,
                            disabledContentColor = White
                        )
                    ) { Text("X", fontWeight = FontWeight.Bold) }
                    Button(
                        modifier = modifier.height(50.dp).offset(10.dp,(-10).dp),
                        onClick = {
                            if (cityState == true && viewModel.cidadeMap.value.isNotBlank()) {
                                val city = City(
                                    name = viewModel.cidadeMap.value,

                                    )
                                favoritosViewModel.addFavoriteCity(city)
                                isFavorite = true
                            } else if (cityShow == true && viewModel.cidadeSearch.value.isNotBlank()) {
                                favoritosViewModel.addFavoriteCityByName(viewModel.cidadeSearch.value)
                                isFavorite = true
                            }
                        }
                    ){
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite icon",
                            tint = if (isFavorite) Red else White
                        )
                    }
                }
                Row{

                    Text(viewModel.cidadeMap.value, fontWeight = FontWeight.Bold, fontSize = 20.sp,
                        modifier = Modifier
                            .offset(0.dp,0.dp))
                    if (viewModel.iconeClimaMap.value.isNotEmpty()) {
                        AsyncImage(
                            model = viewModel.iconeClimaMap.value,
                            contentDescription = "Weather icon",
                            modifier = Modifier
                                .size(35.dp)
                                .offset(0.dp,(-5).dp)

                        )
                }

            }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Temperature (ºC)", color = GrayD)
                        Text(viewModel.temperaturaMap.value, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Humidity (%)", color = GrayD)
                        Text(viewModel.umidadeMap.value, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Rain (mm/h)", color = GrayD)
                        Text(viewModel.chuvaMap.value, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Wind (km/h)", color = GrayD)
                        Text(viewModel.ventoMap.value, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("UV Index", color = GrayD)
                Text(viewModel.uvMap.value, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row{
                    ExposedDropdownMenuBox(
                        expanded = expandedDay,
                        onExpandedChange = { expandedDay = !expandedDay },
                        modifier = modifier.width(105.dp).background(color = Color.Transparent)
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
                        modifier = modifier.width(125.dp).offset(45.dp,0.dp).background(color = Color.Transparent)
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
                            containerColor = White,
                            modifier = modifier.background(color = Color.Transparent)
                        ) {
                            optionsMonth.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        selectedMonth = selectionOption
                                        expandedMonth = false
                                    }
                                )
                            }
                        }
                    }
                }

                Button(
                    colors = ButtonColors(
                        containerColor = GreenL,
                        contentColor = White,
                        disabledContainerColor = GreenL,
                        disabledContentColor = GreenL,
                    ),
                    modifier = modifier.height(50.dp).offset(85.dp,30.dp).border(3.dp, GreenL, RoundedCornerShape(25.dp)),
                    onClick = {
                    },
                ) {
                    Text("Forecast")
                }
                if (geminiResponse.isNotEmpty() ) {
                    val BrokenLines = countBrokenLines(geminiResponse)
                    Box(
                        modifier = Modifier

                            .offset(45.dp, 45.dp)
                            .background( brush = Brush.verticalGradient( // Or Brush.horizontalGradient, Brush.linearGradient
                                colors = listOf(
                                    Color(0xFF64B5F6), // Start color (light blue)
                                    Color(0xFF0D47A1)  // End color (dark blue)
                                )
                            ), shape = RoundedCornerShape(15.dp))
                            .border(0.dp, color = GrayL, shape = RoundedCornerShape(15.dp))
                            .height(125.dp)
                            .width(175.dp)
                            .size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row{

                            val extraDays = (1..5).map { today.plusDays(it.toLong()).dayOfMonth.toString()+"/"+today.plusDays(it.toLong()).monthValue.toString() }
                            if(geminiResponse.contains("<img")
                                ||geminiResponse.contains("https")
                                ||geminiResponse.contains("Ensolarado")
                                ||geminiResponse.contains("Nublado")
                                ||geminiResponse.contains(".png")
                                ||BrokenLines < 5
                                ){showFirstForecastMap = true
                            } else {
                                Text(
                                    text = geminiResponse,
                                    fontSize = 16.sp,
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 5
                                )
                            }

                            Text(
                                text = extraDays.toString().replace("["," ").replace("]","").replace(",","\n"),
                                fontSize = 16.sp,
                                color = White,
                                fontWeight = FontWeight.Bold

                            )
                        }

                    }
                }

            }

        }else if((CityName != "" && cityShow == true)){

            if(showFirstForecast == true){
                fetchHistoricalDataSearch()
                showFirstForecast = false
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(brush = Brush.verticalGradient( // Or Brush.horizontalGradient, Brush.linearGradient
                        colors = listOf(
                            White, // Start color (light blue)
                            Color(0xFF0D47A1)  // End color (dark blue)
                        )
                    ), shape = RoundedCornerShape(15.dp))
                    .border(3.dp, color = Color.Transparent, RoundedCornerShape(12.dp))
                    .padding(48.dp).height(600.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Row{
                    Button(onClick = {
                        cityState = false;
                        cityShow = false;
                        CitySearch = true;
                        showFirstForecastMap = false;
                    },
                        modifier = modifier.height(50.dp).offset(0.dp,(-10).dp),
                        colors = ButtonColors(
                            containerColor = Red,
                            contentColor = White,
                            disabledContainerColor = Red,
                            disabledContentColor = White
                        )
                    ) { Text("X", fontWeight = FontWeight.Bold) }
                    Button(
                        modifier = modifier.height(50.dp).offset(10.dp,(-10).dp),
                        onClick = {
                            Log.d(toString(),viewModel.cidadeSearch.value)

                            if (cityState == true && viewModel.cidadeMap.value.isNotBlank()) {
                                val city = City(
                                    name = viewModel.cidadeMap.value,

                                )
                                favoritosViewModel.addFavoriteCity(city)
                                isFavorite = true
                            } else if (cityShow == true && viewModel.cidadeSearch.value.isNotBlank()) {
                                favoritosViewModel.addFavoriteCityByName(viewModel.cidadeSearch.value)
                                isFavorite = true
                            }
                        }
                    ){
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite icon",
                            tint = if (isFavorite) Red else White
                        )
                    }
                }
                Row{


                    Text(viewModel.cidadeSearch.value, fontWeight = FontWeight.Bold, fontSize = 20.sp,
                        modifier = Modifier.offset(0.dp,0.dp))
                    if (viewModel.iconeClimaSearch.value.isNotEmpty()) {
                        AsyncImage(
                            model = viewModel.iconeClimaSearch.value,
                            contentDescription = "Weather icon",
                            modifier = Modifier
                                .size(35.dp)
                                .offset(0.dp,(-5).dp)

                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Temperature (ºC)", color = GrayD)
                        Text(viewModel.temperaturaSearch.value, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Humidity (%)", color = GrayD)
                        Text(viewModel.umidadeSearch.value, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Rain (mm/h)", color = GrayD)
                        Text(viewModel.chuvaSearch.value, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Wind (km/h)", color = GrayD)
                        Text(viewModel.ventoSearch.value, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("UV Index", color = GrayD)
                Text(viewModel.uvSearch.value, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row{
                    ExposedDropdownMenuBox(
                        expanded = expandedDay,
                        onExpandedChange = { expandedDay = !expandedDay },
                        modifier = modifier.width(105.dp).background(color = Color.Transparent)
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
                        modifier = modifier.width(125.dp).offset(45.dp,0.dp).background(color = Color.Transparent)
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
                            containerColor = White,
                            modifier = modifier.background(color = Color.Transparent)
                        ) {
                            optionsMonth.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        selectedMonth = selectionOption
                                        expandedMonth = false
                                    }
                                )
                            }
                        }
                    }
                }

                Button(
                    colors = ButtonColors(
                        containerColor = GreenL,
                        contentColor = White,
                        disabledContainerColor = GreenL,
                        disabledContentColor = GreenL,
                    ),
                    modifier = modifier.height(50.dp).offset(85.dp,30.dp).border(3.dp, GreenL, RoundedCornerShape(25.dp)),
                    onClick = {
                    },
                ) {
                    Text("Forecast")
                }

                    if (geminiResponse.isNotEmpty() ) {
                        Box(
                            modifier = Modifier

                                .offset(45.dp, 45.dp)
                                .background( brush = Brush.verticalGradient( // Or Brush.horizontalGradient, Brush.linearGradient
                                    colors = listOf(
                                        Color(0xFF64B5F6), // Start color (light blue)
                                        Color(0xFF0D47A1)  // End color (dark blue)
                                    )
                                ), shape = RoundedCornerShape(15.dp))
                                .border(0.dp, color = GrayL, shape = RoundedCornerShape(15.dp))
                                .height(125.dp)
                                .width(175.dp)
                                .size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row{
                                val extraDays = (1..5).map { today.plusDays(it.toLong()).dayOfMonth.toString()+"/"+today.plusDays(it.toLong()).monthValue.toString() }
                                if(geminiResponse.contains("<img")
                                    ||geminiResponse.contains("https")
                                    ||geminiResponse.contains(".png")
                                    ||geminiResponse.contains("png")
                                    ||geminiResponse.contains("Ensolarado")
                                    ||geminiResponse.contains("Nublado")
                                    ||geminiResponse.length < 15
                                    ||geminiResponse.length < 20
                                    ||geminiResponse.length < 30){showFirstForecast = true
                                } else {
                                    Text(
                                        text = geminiResponse,
                                        fontSize = 16.sp,
                                        color = White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 5
                                    )
                                }

                                Text(
                                    text = extraDays.toString().replace("["," ").replace("]","").replace(",","\n"),
                                    fontSize = 16.sp,
                                    color = White,
                                    fontWeight = FontWeight.Bold

                                )
                            }

                        }
                    }

            }

        }

    }
}