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

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally


import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.weatheralert.BuildConfig
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

import com.weatheralert.ui.theme.GrayD
import com.weatheralert.ui.theme.GrayL
import com.weatheralert.ui.theme.GreenL
import com.weatheralert.ui.theme.Red
import com.weatheralert.ui.theme.White
import java.net.URL


@ExperimentalMaterial3Api
@Composable
fun MapPage(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentTime = LocalDate.now()
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val cameraPositionState = rememberCameraPositionState()
    val modelIa = GenerativeModel(modelName = "gemini-pro", apiKey = BuildConfig.GEMINI_API_KEY)
    var cityLong by rememberSaveable { mutableStateOf<Double?>(null) }
    var cityLati by rememberSaveable { mutableStateOf<Double?>(null) }
    var cityState by rememberSaveable { mutableStateOf<Boolean?>(false) }
    var expandedDay by remember { mutableStateOf(false) }
    var expandedMap by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf("Day") }
    var selectedMonth by remember { mutableStateOf("Month") }
    var selectedMap by remember { mutableStateOf("Select map") }
    val optionsDay: List<String> = (1..31).map { it.toString() }
    val optionsMonth: List<String> = (1..12).map { it.toString() }
    val optionsMap = listOf("Normal","MODIS Terra True Color")
    var map1Chooser by remember { mutableStateOf(true) }  // ← CORRETO
    var map2Chooser by remember { mutableStateOf(false) } // ← CORRETO

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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMap) },
                    modifier = Modifier.background(color = Color.Transparent).menuAnchor()
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
                                Log.d(String.toString(),selectedMap)
                                if(selectionOption == "Select map" || selectionOption == "Normal"){
                                    map1Chooser = true
                                    map2Chooser = false

                                    Log.d(String.toString(), map1Chooser.toString())
                                    Log.d(String.toString(), map2Chooser.toString())

                                }

                                if(selectionOption == "MODIS Terra True Color"){
                                    map1Chooser = false
                                    map2Chooser = true

                                    Log.d(String.toString(), map1Chooser.toString())
                                    Log.d(String.toString(), map2Chooser.toString())

                                }
                            },

                            )
                    }
                }
            }
        }
        // --- Box de informações meteorológicas ---
        if (cityLati != null && cityLong != null && cityState == true) {


            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(color = White, shape = RoundedCornerShape(12.dp))
                    .border(3.dp, GrayD, RoundedCornerShape(12.dp))
                    .padding(48.dp).height(600.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    cityState = false;
                },
                    modifier = modifier.height(50.dp).offset(0.dp,(-10).dp),
                    colors = ButtonColors(
                        containerColor = Red,
                        contentColor = White,
                        disabledContainerColor = Red,
                        disabledContentColor = White
                    )
                ) { Text("X", fontWeight = FontWeight.Bold) }
                Row{
                    Text(viewModel.cidadeMap.value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    if (viewModel.iconeClimaMap.value.isNotEmpty()) {
                        AsyncImage(
                            model = viewModel.iconeClimaMap.value,
                            contentDescription = "Weather icon",
                            modifier = Modifier
                                .size(40.dp)
                                .offset(0.dp,(-15).dp)

                        )
                }

            }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Temperature (ºC)", color = GrayL)
                        Text(viewModel.temperaturaMap.value, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Humidity (%)", color = GrayL)
                        Text(viewModel.umidadeMap.value, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Rain (mm/h)", color = GrayL)
                        Text(viewModel.chuvaMap.value, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Wind (km/h)", color = GrayL)
                        Text(viewModel.ventoMap.value, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("UV Index", color = GrayL)
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
                            modifier = Modifier.background(color = Color.Transparent).menuAnchor()
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
                            modifier = Modifier.background(color = Color.Transparent).menuAnchor()
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


            }

        }


    }
}