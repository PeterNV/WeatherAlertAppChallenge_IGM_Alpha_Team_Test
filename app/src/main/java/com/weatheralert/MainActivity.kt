package com.weatheralert

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
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
import java.time.LocalDate
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.weatheralert.ui.nav.BottomNavBar
import com.weatheralert.ui.nav.BottomNavItem
import com.weatheralert.ui.nav.MainNavHost
import com.weatheralert.ui.theme.GrayL
import com.weatheralert.ui.theme.WeatherAlertTheme
import com.weatheralert.ui.theme.White
import coil.compose.AsyncImage
import com.google.ai.client.generativeai.GenerativeModel

import com.weatheralert.ui.theme.GreenL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.format.DateTimeFormatter
import java.util.Locale


class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //val context = LocalContext.current

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            val context = LocalContext.current
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = {}
            )
            val viewModel: MainViewModel = viewModel()
            val navController = rememberNavController()

            WeatherAlertTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val items = listOf(
                            BottomNavItem.HomeButton,
                            BottomNavItem.ListButton,
                            BottomNavItem.MapButton,
                        )
                        BottomNavBar(viewModel, items)
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        // Solicitar permissão
                        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

                        // Atualiza a localização assim que o Composable aparecer
                        LaunchedEffect(Unit) {
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                fusedLocationClient.getCurrentLocation(
                                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                                    null
                                ).addOnSuccessListener { location ->
                                    location?.let {
                                        viewModel.getYourLocation(it.latitude, it.longitude)
                                    }
                                }
                            }
                        }

                        HomePage(viewModel = mainViewModel)
                        MainNavHost(navController = navController, viewModel = viewModel)

                        // Navegação baseada no page do ViewModel
                        LaunchedEffect(viewModel.page.value) {
                            navController.navigate(viewModel.page.value) {
                                navController.graph.startDestinationRoute?.let {
                                    popUpTo(it) { saveState = true }
                                    restoreState = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    val currentTime = LocalDate.now()
    val modelIa = GenerativeModel(modelName = "gemini-2.0-flash", apiKey = BuildConfig.GEMINI_API_KEY)
    val context = LocalContext.current

    var expandedDay by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf("Day") }
    var selectedMonth by remember { mutableStateOf("Month") }
    var weatherDataSize by remember { mutableStateOf(18.sp) }
    val optionsDay: List<String> = (1..31).map { it.toString() }
    val optionsMonth: List<String> = (1..12).map { it.toString() }

    // Estado para armazenar dados históricos
    var historicalWeatherData by remember { mutableStateOf("") }

    // Efeito para buscar dados históricos quando a cidade mudar
    LaunchedEffect(viewModel.cidade.value) {
        if (viewModel.cidade.value.isNotBlank()) {
            fetchHistoricalWeatherData(viewModel.cidade.value, context) { data ->
                historicalWeatherData = data
            }
        }
    }

    // === 3. Datas ===
    val today = java.time.LocalDate.now()
    val sevenDaysAgo = today.minusDays(7)

    val startDate = sevenDaysAgo.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
    val endDate = today.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
    val params = "T2M,RH2M,PRECTOTCORR,WS10M,ALLSKY_KT"
    var geminiResponse by remember { mutableStateOf("") }
    // Função para buscar dados históricos
    var isLoading by remember { mutableStateOf(false) }
    val fetchHistoricalData: () -> Unit = {
        if (viewModel.cidade.value.isNotBlank()) {
            isLoading = true
            geminiResponse = "Gerando previsão..."

            fetchHistoricalWeatherData(viewModel.cidade.value, context) { data ->
                historicalWeatherData = data

                // Agora gerar o conteúdo com Gemini
                val weatherPrompt1 = "Considerando que estou em ${viewModel.cidade.value}, hoje é $currentTime, " +
                        "os dados de hoje são ${viewModel.temperatura.value}ºC, ${viewModel.umidade.value}%, " +
                        "${viewModel.chuva.value}mm/h, ${viewModel.vento.value}km/h e índice de uv: ${viewModel.uv.value}"

                val weatherPrompt2 = "Dados históricos dos últimos 7 dias: $historicalWeatherData. " +
                        "Gere apenas 10 valores 5 da temperatura em ºC e 5 dos próximos 5 dias, e não inclua mais textos além disso. Exemplo do que você deve gerar: " +
                        "27,5ºC 28,1ºC 25,1ºC 26,1ºC 23,1ºC"

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

    Box(modifier = modifier.fillMaxSize()){
        Image(
            painter = painterResource(id = R.drawable.imagem_1_login_menu),
            contentDescription = "Main background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

    }
    Column(

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally,
        modifier = modifier
            .padding(16.dp, 35.dp)
            .fillMaxSize()
            .shadow(5.dp, RoundedCornerShape(25.dp))
            .border(2.dp, White, RoundedCornerShape(25.dp))
            .background(White, RoundedCornerShape(25.dp))
    ) {
        Row{

            Text(
                viewModel.cidade.value, fontWeight = FontWeight.Bold, fontSize = 30.sp,
                modifier = modifier.offset(0.dp,(-155).dp)
            )
            if (viewModel.iconeClima.value.isNotEmpty()) {
                AsyncImage(
                    model = viewModel.iconeClima.value,
                    contentDescription = "Weather icon",
                    modifier = Modifier
                        .size(40.dp)
                        .offset(10.dp, (-155).dp)
                )
            }
        }
        Row{
                Text("Temperature (ºC) ", fontWeight = FontWeight.Bold, color = GrayL, fontSize = weatherDataSize,
                    modifier = modifier.offset((-45).dp,(-125).dp))
                Text("Humidity (%)", fontWeight = FontWeight.Bold,  color = GrayL, fontSize = weatherDataSize,
                    modifier = modifier.offset(45.dp,(-125).dp))
        }
        Row{
            Text(
                viewModel.temperatura.value, fontWeight = FontWeight.Bold, fontSize = weatherDataSize,
                modifier = modifier.offset((-85).dp,(-115).dp))
            Text(
                viewModel.umidade.value, fontWeight = FontWeight.Bold, fontSize = weatherDataSize,
                modifier = modifier.offset((85).dp,(-115).dp))
        }
        Row{
            Text("Rain (mm/h) ", fontWeight = FontWeight.Bold, color = GrayL, fontSize = weatherDataSize,
                modifier = modifier.offset((-45).dp,(-105).dp))
            Text("Wind (km/h)", fontWeight = FontWeight.Bold, color = GrayL, fontSize = weatherDataSize,
                modifier = modifier.offset(65.dp,(-105).dp))
        }
        Row{
            Text(
                viewModel.chuva.value, fontWeight = FontWeight.Bold, fontSize = weatherDataSize,
                modifier = modifier.offset((-80).dp,(-95).dp))
            Text(
                viewModel.vento.value, fontWeight = FontWeight.Bold, fontSize = weatherDataSize,
                modifier = modifier.offset((85).dp,(-95).dp))
        }
        Text("Uv",  fontWeight = FontWeight.Bold, color = GrayL, fontSize = weatherDataSize,
            modifier = modifier.offset((0).dp,(-85).dp))
        Text(
            viewModel.uv.value,  fontWeight = FontWeight.Bold, fontSize = weatherDataSize,
            modifier = modifier.offset((0).dp,(-85).dp))
        Row{
            ExposedDropdownMenuBox(
                expanded = expandedDay,
                onExpandedChange = { expandedDay = !expandedDay },
                modifier = modifier.width(105.dp).offset((-40).dp,(-60).dp).background(color = Color.Transparent)
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
                modifier = modifier.width(125.dp).offset(45.dp,(-60).dp).background(color = Color.Transparent)
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
            modifier = modifier
                .height(50.dp)
                .offset((-8).dp,(-15).dp)
                .border(3.dp, GreenL, RoundedCornerShape(25.dp)),
            onClick = {
                fetchHistoricalData()
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                Text("Processando...", fontSize = 16.sp)
            } else {
                Text("Forecast", fontSize = 16.sp)
            }
        }

        val extraDays = (1..5).map { today.plusDays(it.toLong()).dayOfMonth.toString()+"/"+today.plusDays(it.toLong()).monthValue.toString() }
        Log.e("XR_LIST", extraDays.toString())

        if (geminiResponse.isNotEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(0.dp, 25.dp)
                    .background(color = GrayL, shape = RoundedCornerShape(15.dp))
                    .border(0.dp, color = GrayL, shape = RoundedCornerShape(15.dp)).height(65.dp),
                contentAlignment = Alignment.Center // <-- Centraliza o Text dentro do Box
            ) {
                Text(
                    text = geminiResponse +extraDays.toString().replace("[","   ").replace("]","  ").replace(",","  "),
                    fontSize = 16.sp,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

    }

}

private fun fetchHistoricalWeatherData(cityName: String, context: Context, callback: (String) -> Unit) {
    // Primeiro, precisamos geocodificar o nome da cidade para coordenadas
    val geocoder = Geocoder(context, Locale.getDefault())

    try {
        val addresses = geocoder.getFromLocationName(cityName, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            val lat = address.latitude
            val lon = address.longitude

            // Agora buscar dados da NASA POWER API com as coordenadas
            val today = LocalDate.now()
            val sevenDaysAgo = today.minusDays(7)
            val startDate = sevenDaysAgo.format(DateTimeFormatter.BASIC_ISO_DATE)
            val endDate = today.format(DateTimeFormatter.BASIC_ISO_DATE)
            val params = "T2M,RH2M,PRECTOTCORR,WS10M,ALLSKY_KT"

            val powerUrl = "https://power.larc.nasa.gov/api/temporal/daily/point?" +
                    "parameters=$params&community=RE&longitude=$lon&latitude=$lat" +
                    "&start=$startDate&end=$endDate&format=JSON"

            // Executar em uma thread em background
            Thread {
                try {
                    val connection = URL(powerUrl).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val jsonData = JSONObject(response)

                        if (jsonData.has("properties")) {
                            val properties = jsonData.getJSONObject("properties")
                            val parameter = properties.getJSONObject("parameter")

                            // Processar dados para criar um resumo
                            val summary = StringBuilder()

                            // Temperatura média (T2M)
                            val temperatures = parameter.getJSONObject("T2M")
                            var tempSum = 0.0
                            var tempCount = 0
                            for (key in temperatures.keys()) {
                                tempSum += temperatures.getDouble(key)
                                tempCount++
                            }
                            val avgTemp = tempSum / tempCount
                            summary.append("Temperatura média: ${"%.1f".format(avgTemp)}°C. ")

                            // Umidade (RH2M)
                            val humidity = parameter.getJSONObject("RH2M")
                            var humiditySum = 0.0
                            var humidityCount = 0
                            for (key in humidity.keys()) {
                                humiditySum += humidity.getDouble(key)
                                humidityCount++
                            }
                            val avgHumidity = humiditySum / humidityCount
                            summary.append("Umidade média: ${"%.1f".format(avgHumidity)}%. ")

                            // Precipitação (PRECTOTCORR)
                            val precipitation = parameter.getJSONObject("PRECTOTCORR")
                            var precipSum = 0.0
                            for (key in precipitation.keys()) {
                                precipSum += precipitation.getDouble(key)
                            }
                            summary.append("Precipitação total: ${"%.1f".format(precipSum)}mm. ")

                            // Velocidade do vento (WS10M)
                            val windSpeed = parameter.getJSONObject("WS10M")
                            var windSum = 0.0
                            var windCount = 0
                            for (key in windSpeed.keys()) {
                                windSum += windSpeed.getDouble(key)
                                windCount++
                            }
                            val avgWindSpeed = windSum / windCount
                            summary.append("Velocidade média do vento: ${"%.1f".format(avgWindSpeed)}m/s.")

                            callback(summary.toString())
                        } else {
                            callback("Dados históricos não disponíveis para esta localização.")
                        }
                    } else {
                        callback("Erro ao buscar dados históricos: Código $responseCode")
                    }
                } catch (e: Exception) {
                    callback("Erro ao processar dados históricos: ${e.message}")
                }
            }.start()
        } else {
            callback("Localização não encontrada: $cityName")
        }
    } catch (e: Exception) {
        callback("Erro ao geocodificar cidade: ${e.message}")
    }
}