package com.weatheralert

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.graphics.Brush
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
import com.weatheralert.ui.nav.BottomNavBar
import com.weatheralert.ui.nav.BottomNavItem
import com.weatheralert.ui.nav.MainNavHost
import com.weatheralert.ui.theme.GrayL
import com.weatheralert.ui.theme.WeatherAlertTheme
import com.weatheralert.ui.theme.White
import coil.compose.AsyncImage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.gms.location.Priority
import com.weatheralert.ui.theme.GrayD
import com.weatheralert.ui.theme.GreenL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val favoritosViewModel: FavoritosViewModel by viewModels()
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
                                    Priority.PRIORITY_HIGH_ACCURACY,
                                    null
                                ).addOnSuccessListener { location ->
                                    location?.let {
                                        viewModel.getYourLocation(it.latitude, it.longitude)
                                    }
                                }
                            }
                        }

                        HomePage(viewModel = mainViewModel)
                        MainNavHost(navController = navController, viewModel = viewModel,
                            favoritosViewModel = favoritosViewModel)

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
    val today = LocalDate.now()
    val modelIa = GenerativeModel(modelName = "gemini-2.0-flash", apiKey = BuildConfig.GEMINI_API_KEY)
    val context = LocalContext.current

    var expandedDay by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }
    var showFirstForecast by remember { mutableStateOf(true) }
    var selectedDay by remember { mutableStateOf("Day") }
    var selectedMonth by remember { mutableStateOf("Month") }
    var weatherDataSize by remember { mutableStateOf(18.sp) }
    val optionsDay: List<String> = (today.dayOfMonth..31).map { it.toString() }
    val optionsMonth: List<String> = (today.monthValue..12).map { it.toString() }

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

    var geminiResponse by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    val fetchHistoricalData: () -> Unit = {
        if (viewModel.cidade.value.isNotBlank()) {
            isLoading = true
            geminiResponse = ""

            fetchHistoricalWeatherData(viewModel.cidade.value, context) { data ->
                historicalWeatherData = data

                // Agora gerar o conteúdo com Gemini
                val weatherPrompt1 = "Considerando que estou em ${viewModel.cidade.value}, hoje é $today, " +
                        "os dados de hoje são ${viewModel.temperatura.value}ºC, ${viewModel.umidade.value}%, " +
                        "${viewModel.chuva.value}mm/h, ${viewModel.vento.value}km/h e índice de uv: ${viewModel.uv.value}"

                val weatherPrompt2 = "Dados históricos dos últimos 7 dias: $historicalWeatherData. " +
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
            .border(2.dp, Color.Transparent, RoundedCornerShape(25.dp))
            .background(brush = Brush.verticalGradient( // Or Brush.horizontalGradient, Brush.linearGradient
                colors = listOf(
                    White, // Start color (light blue)
                    Color(0xFF0D47A1)  // End color (dark blue)
                )
            ), shape = RoundedCornerShape(15.dp))
    ) {
        Row{

            Text(
                viewModel.cidade.value, fontWeight = FontWeight.Bold, fontSize = 30.sp,
                modifier = modifier.offset(0.dp,(-105).dp)
            )
            if (viewModel.iconeClima.value.isNotEmpty()) {
                AsyncImage(
                    model = viewModel.iconeClima.value,
                    contentDescription = "Weather icon",
                    modifier = Modifier
                        .size(40.dp)
                        .offset(10.dp, (-105).dp)
                )
            }
        }
        Row{
                Text("Temperature (ºC) ", fontWeight = FontWeight.Bold, color = GrayD, fontSize = weatherDataSize,
                    modifier = modifier.offset((-45).dp,(-85).dp))
                Text("Humidity (%)", fontWeight = FontWeight.Bold,  color = GrayD, fontSize = weatherDataSize,
                    modifier = modifier.offset(45.dp,(-85).dp))
        }
        Row{
            Text(
                viewModel.temperatura.value, fontWeight = FontWeight.Bold, fontSize = weatherDataSize,
                modifier = modifier.offset((-85).dp,(-75).dp))
            Text(
                viewModel.umidade.value, fontWeight = FontWeight.Bold, fontSize = weatherDataSize,
                modifier = modifier.offset((85).dp,(-75).dp))
        }
        Row{
            Text("Rain (mm/h) ", fontWeight = FontWeight.Bold, color = GrayD, fontSize = weatherDataSize,
                modifier = modifier.offset((-45).dp,(-55).dp))
            Text("Wind (km/h)", fontWeight = FontWeight.Bold, color = GrayD, fontSize = weatherDataSize,
                modifier = modifier.offset(65.dp,(-55).dp))
        }
        Row{
            Text(
                viewModel.chuva.value, fontWeight = FontWeight.Bold, fontSize = weatherDataSize,
                modifier = modifier.offset((-80).dp,(-45).dp))
            Text(
                viewModel.vento.value, fontWeight = FontWeight.Bold, fontSize = weatherDataSize,
                modifier = modifier.offset((85).dp,(-45).dp))
        }
        Text("Uv",  fontWeight = FontWeight.Bold, color = GrayD, fontSize = weatherDataSize,
            modifier = modifier.offset((0).dp,(-15).dp))
        Text(
            viewModel.uv.value,  fontWeight = FontWeight.Bold, fontSize = weatherDataSize,
            modifier = modifier.offset((0).dp,(-15).dp))
        Row{

            ExposedDropdownMenuBox(
                expanded = expandedDay,
                onExpandedChange = { expandedDay = !expandedDay },
                modifier = modifier.width(105.dp).offset((-40).dp,(-20).dp).background(color = Color.Transparent)
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
                modifier = modifier.width(125.dp).offset(45.dp,(-20).dp),

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

        Button(
            colors = ButtonColors(
                containerColor = GreenL,
                contentColor = White,
                disabledContainerColor = GreenL,
                disabledContentColor = GreenL,
            ),
            modifier = modifier
                .height(50.dp)
                .offset((-8).dp,(15).dp)
                .border(3.dp, GreenL, RoundedCornerShape(25.dp)),
            onClick = {

            },

        ) {

                Text("Forecast", fontSize = 16.sp)

        }

        val extraDays = (1..5).map { today.plusDays(it.toLong()).dayOfMonth.toString()+"/"+today.plusDays(it.toLong()).monthValue.toString() }
        Log.e("XR_LIST", extraDays.toString())
        if(showFirstForecast == true){
            fetchHistoricalData()
            showFirstForecast = false
        }

        if (geminiResponse.isNotEmpty() ) {
            Box(
                modifier = Modifier

                    .offset(0.dp, 45.dp)
                    .background( brush = Brush.verticalGradient( // Or Brush.horizontalGradient, Brush.linearGradient
                        colors = listOf(
                            Color(0xFF64B5F6), // Start color (light blue)
                            Color(0xFF0D47A1)  // End color (dark blue)
                        )
                    ), shape = RoundedCornerShape(15.dp))
                    .border(0.dp, color = GrayL, shape = RoundedCornerShape(15.dp))
                    .height(125.dp)
                    .width(175.dp),
                contentAlignment = Alignment.Center
            ) {
            Row{
                if(geminiResponse.contains("<img")
                    ||geminiResponse.contains("https")
                    ||geminiResponse.contains("Ensolarado")
                    ||geminiResponse.contains("Nublado")
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