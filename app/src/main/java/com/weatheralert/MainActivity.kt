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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
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


class MainActivity : ComponentActivity() {

    val mainViewModel: MainViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                100
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mainViewModel.getYourLocation(currentLatLng.latitude, currentLatLng.longitude)
                } else {
                    // Nenhuma localização encontrada
                }
            }

        setContent {
            val launcher = rememberLauncherForActivityResult(contract =
            ActivityResultContracts.RequestPermission(), onResult = {} )
            val viewModel : MainViewModel = viewModel()
            val navController = rememberNavController()
            WeatherAlertTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                    bottomBar =  {
                        val items = listOf(
                            BottomNavItem.HomeButton,
                            BottomNavItem.ListButton,
                            BottomNavItem.MapButton,
                        )

                        BottomNavBar(viewModel, items)
                    }) {innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        HomePage(

                            viewModel = mainViewModel
                        )
                        MainNavHost(navController = navController, viewModel = viewModel)
                    }
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



@Composable
fun HomePage(modifier: Modifier = Modifier, viewModel: MainViewModel) {
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
            .padding(16.dp, 185.dp)
            .fillMaxSize()
            .shadow(5.dp, RoundedCornerShape(25.dp))
            .border(2.dp, White, RoundedCornerShape(25.dp))
            .background(White, RoundedCornerShape(25.dp))
    ) {
        Row{
            Text("City: ", fontWeight = FontWeight.Bold, fontSize = 30.sp,
                modifier = modifier.offset(0.dp,(-85).dp)
            )
            Text("${viewModel.cidade.value}", fontWeight = FontWeight.Bold, fontSize = 30.sp,
                modifier = modifier.offset(0.dp,(-85).dp)
            )
        }
        Row{
                Text("Temperature (ºC) ", fontWeight = FontWeight.Bold, color = GrayL,
                    modifier = modifier.offset((-45).dp,(-45).dp))
                Text("Humidity (%)", fontWeight = FontWeight.Bold,  color = GrayL,
                    modifier = modifier.offset(45.dp,(-45).dp))
        }
        Row{
            Text("${viewModel.temperatura.value}", fontWeight = FontWeight.Bold,
                modifier = modifier.offset((-75).dp,(-20).dp))
            Text("${viewModel.umidade.value}", fontWeight = FontWeight.Bold,
                modifier = modifier.offset((85).dp,(-20).dp))
        }
        Row{
            Text("Rain (mm/h) ", fontWeight = FontWeight.Bold, color = GrayL,
                modifier = modifier.offset((-45).dp))
            Text("Wind (km/h)", fontWeight = FontWeight.Bold, color = GrayL,
                modifier = modifier.offset(65.dp))
        }
        Row{
            Text("${viewModel.chuva.value}", fontWeight = FontWeight.Bold,
                modifier = modifier.offset((-50).dp,20.dp))
            Text("${viewModel.vento.value}", fontWeight = FontWeight.Bold,
                modifier = modifier.offset((75).dp,20.dp))
        }
        Text("Uv",  fontWeight = FontWeight.Bold, color = GrayL,
            modifier = modifier.offset((0).dp,40.dp))
        Text("${viewModel.uv.value}",  fontWeight = FontWeight.Bold,
            modifier = modifier.offset((0).dp,45.dp))
    }
}

