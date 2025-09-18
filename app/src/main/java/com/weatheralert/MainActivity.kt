package com.weatheralert

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
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
import androidx.compose.ui.draw.clip
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
import com.weatheralert.ui.theme.GreenL
import kotlin.math.pow

import java.util.concurrent.TimeUnit

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
    var expandedDay by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf("Day") }
    var selectedMonth by remember { mutableStateOf("Month") }

    val optionsDay: List<String> = (1..31).map { it.toString() }
    val optionsMonth: List<String> = (1..12).map { it.toString() }

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
                Text("Temperature (ºC) ", fontWeight = FontWeight.Bold, color = GrayL,
                    modifier = modifier.offset((-45).dp,(-125).dp))
                Text("Humidity (%)", fontWeight = FontWeight.Bold,  color = GrayL,
                    modifier = modifier.offset(45.dp,(-125).dp))
        }
        Row{
            Text(
                viewModel.temperatura.value, fontWeight = FontWeight.Bold,
                modifier = modifier.offset((-85).dp,(-115).dp))
            Text(
                viewModel.umidade.value, fontWeight = FontWeight.Bold,
                modifier = modifier.offset((85).dp,(-115).dp))
        }
        Row{
            Text("Rain (mm/h) ", fontWeight = FontWeight.Bold, color = GrayL,
                modifier = modifier.offset((-45).dp,(-105).dp))
            Text("Wind (km/h)", fontWeight = FontWeight.Bold, color = GrayL,
                modifier = modifier.offset(65.dp,(-105).dp))
        }
        Row{
            Text(
                viewModel.chuva.value, fontWeight = FontWeight.Bold,
                modifier = modifier.offset((-80).dp,(-95).dp))
            Text(
                viewModel.vento.value, fontWeight = FontWeight.Bold,
                modifier = modifier.offset((85).dp,(-95).dp))
        }
        Text("Uv",  fontWeight = FontWeight.Bold, color = GrayL,
            modifier = modifier.offset((0).dp,(-85).dp))
        Text(
            viewModel.uv.value,  fontWeight = FontWeight.Bold,
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
            modifier = modifier.height(50.dp).offset((-8).dp,(-15).dp).border(3.dp, GreenL, RoundedCornerShape(25.dp)),
            onClick = {
            },
        ) {
            Text("Forecast", fontSize = 18.sp)
        }
    }
}