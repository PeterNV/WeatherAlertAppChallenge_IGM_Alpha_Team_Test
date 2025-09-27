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

import java.net.URL


@ExperimentalMaterial3Api
@Composable
fun MapPage(modifier: Modifier = Modifier, viewModel: MainViewModel,favoritosViewModel: FavoritesViewModel) {
    val context = LocalContext.current
    val currentTime = LocalDate.now()
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val cameraPositionState = rememberCameraPositionState()

    var cityLong by rememberSaveable { mutableStateOf<Double?>(null) }
    var cityLati by rememberSaveable { mutableStateOf<Double?>(null) }
    var cityState by rememberSaveable { mutableStateOf<Boolean?>(false) }
    var expandedMap by remember { mutableStateOf(false) }
    var cityShow by remember { mutableStateOf(false) }
    var selectedMap by remember { mutableStateOf("Select map") }
    var cityName by remember { mutableStateOf("") }
    var citySearch by remember { mutableStateOf(true) }
    val optionsMap = listOf("Normal","MODIS Terra True Color")
    var map1Chooser by remember { mutableStateOf(true) }  // ← CORRETO
    var map2Chooser by remember { mutableStateOf(false) } // ← CORRETO
    var historicalWeatherData by remember { mutableStateOf("") }
    var showFirstForecast by remember { mutableStateOf(false) }
    var showFirstForecastMap by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    // Efeito para buscar dados históricos quando a cidade mudar
    LaunchedEffect(viewModel.cidadeSearch.value) {
        if (viewModel.cidadeSearch.value.isNotBlank()) {
            fetchHistoricalWeatherData(viewModel.cidadeSearch.value, context) { data ->
                historicalWeatherData = data
            }
        }
    }




    Box(modifier = modifier.fillMaxSize()) {

        // --- Google Map ---
        if(map1Chooser){
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
                    cityState = true
                    showFirstForecastMap = true
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

        if(map2Chooser){
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
                    value = cityName,
                    onValueChange = {cityName = it},
                    modifier = modifier.border(3.dp, color = GrayD, shape = RoundedCornerShape(25.dp)).height(55.dp).width(215.dp),
                    placeholder = { Text("City name", color = GrayL) },
                    shape = RoundedCornerShape(25.dp),
                    enabled = citySearch
                )
                Button(
                    onClick = {
                        cityShow = true
                        viewModel.getCity(cityName)
                        citySearch = false
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

            Column(
                modifier = modifier
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
                        cityState = false
                        showFirstForecastMap = false
                        isFavorite = false
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
                            } else if (cityShow && viewModel.cidadeSearch.value.isNotBlank()) {
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
            }

        }else if((cityName != "" && cityShow)){

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
                        cityState = false
                        cityShow = false
                        citySearch = true
                        showFirstForecastMap = false
                        isFavorite = false
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
                            } else if (cityShow && viewModel.cidadeSearch.value.isNotBlank()) {
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

            }
        }
    }
}