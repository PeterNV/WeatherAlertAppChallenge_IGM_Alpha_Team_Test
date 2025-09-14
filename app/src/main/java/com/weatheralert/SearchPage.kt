package com.weatheralert

import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatheralert.ui.theme.GrayD
import com.weatheralert.ui.theme.GrayL
import com.weatheralert.ui.theme.GreenL
import com.weatheralert.ui.theme.White

//@Preview(showBackground = true)
@Composable
fun SearchPage(modifier: Modifier = Modifier, viewModel: MainViewModel){
    var CityName  by rememberSaveable { mutableStateOf("") }


    Column (verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally,
        modifier = modifier.fillMaxSize().background(color = White)) {
        Row(modifier = modifier.offset(0.dp,-(250).dp)){
            OutlinedTextField(
                onValueChange = {CityName = it},
                value = CityName,

                shape = RoundedCornerShape(25.dp),
                placeholder = { Text("City name", color = GrayD, fontSize = 12.sp) },
                modifier = modifier.width(285.dp).height(50.dp).offset(0.dp,40.dp).border(3.dp, GrayL, shape = RoundedCornerShape(25.dp))

            )
            Button(
                colors = ButtonColors(
                    containerColor = GreenL,
                    contentColor = White,
                    disabledContainerColor = GreenL,
                    disabledContentColor = GreenL,
                ),
                modifier = modifier.height(50.dp).offset(5.dp,40.dp).border(3.dp, GreenL, RoundedCornerShape(25.dp)),
                onClick = {

                    viewModel.getYourLocationCity(CityName);

                },
            ) {
                Icon(
                    imageVector = Icons.Filled.Search, // Use a pre-defined Material Icon
                    contentDescription = "Search button" // Provide a content description for accessibility
                )
            }
        }

            Row{
                Text("City: ", fontWeight = FontWeight.Bold, fontSize = 30.sp,
                    modifier = modifier.offset(0.dp,(-85).dp)
                )
                Text("${viewModel.cidadeSearch.value}", fontWeight = FontWeight.Bold, fontSize = 30.sp,
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
                Text("${viewModel.temperaturaSearch.value}", fontWeight = FontWeight.Bold,
                    modifier = modifier.offset((-75).dp,(-20).dp))
                Text("${viewModel.umidadeSearch.value}", fontWeight = FontWeight.Bold,
                    modifier = modifier.offset((85).dp,(-20).dp))
            }
            Row{
                Text("Rain (mm/h) ", fontWeight = FontWeight.Bold, color = GrayL,
                    modifier = modifier.offset((-45).dp))
                Text("Wind (km/h)", fontWeight = FontWeight.Bold, color = GrayL,
                    modifier = modifier.offset(65.dp))
            }
            Row{
                Text("${viewModel.chuvaSearch.value}", fontWeight = FontWeight.Bold,
                    modifier = modifier.offset((-50).dp,20.dp))
                Text("${viewModel.ventoSearch.value}", fontWeight = FontWeight.Bold,
                    modifier = modifier.offset((75).dp,20.dp))
            }
            Text("Uv",  fontWeight = FontWeight.Bold, color = GrayL,
                modifier = modifier.offset((0).dp,40.dp))
            Text("${viewModel.uvSearch.value}",  fontWeight = FontWeight.Bold,
                modifier = modifier.offset((0).dp,45.dp))

    }


}