package com.weatheralert


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatheralert.ui.theme.GrayD
import com.weatheralert.ui.theme.GrayL
import com.weatheralert.ui.theme.GreenL
import com.weatheralert.ui.theme.White

//@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(modifier: Modifier = Modifier, viewModel: MainViewModel){
    var CityName  by rememberSaveable { mutableStateOf("") }
    var expandedDay by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf("Day") }
    var selectedMonth by remember { mutableStateOf("Month") }

    val optionsDay: List<String> = (1..31).map { it.toString() }
    val optionsMonth: List<String> = (1..12).map { it.toString() }

    Column (verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally,
        modifier = modifier.fillMaxSize().background(color = White)) {
        Row(modifier = modifier.offset(0.dp,-(250).dp)){
            OutlinedTextField(
                onValueChange = {CityName = it},
                value = CityName,

                shape = RoundedCornerShape(25.dp),
                placeholder = { Text("City name", color = GrayD, fontSize = 12.sp) },
                modifier = modifier.width(285.dp).height(50.dp).offset(0.dp,60.dp).border(3.dp, GrayL, shape = RoundedCornerShape(25.dp))

            )
            Button(
                colors = ButtonColors(
                    containerColor = GreenL,
                    contentColor = White,
                    disabledContainerColor = GreenL,
                    disabledContentColor = GreenL,
                ),
                modifier = modifier.height(50.dp).offset(5.dp,60.dp).border(3.dp, GreenL, RoundedCornerShape(25.dp)),
                onClick = {

                    viewModel.getCity(CityName);

                },
            ) {
                Icon(
                    imageVector = Icons.Filled.Search, // Use a pre-defined Material Icon
                    contentDescription = "Search button" // Provide a content description for accessibility
                )
            }
        }
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
                    modifier = Modifier.offset((-40).dp,(-120).dp).menuAnchor()
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
                modifier = modifier.width(125.dp).offset(45.dp,(-120).dp)
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
        if(CityName != null){
            Row{
                Text("City: ", fontWeight = FontWeight.Bold, fontSize = 30.sp,
                    modifier = modifier.offset(0.dp,(-85).dp)
                )
                Text(
                    viewModel.cidadeSearch.value, fontWeight = FontWeight.Bold, fontSize = 30.sp,
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
                Text(
                    viewModel.temperaturaSearch.value, fontWeight = FontWeight.Bold,
                    modifier = modifier.offset((-75).dp,(-20).dp))
                Text(
                    viewModel.umidadeSearch.value, fontWeight = FontWeight.Bold,
                    modifier = modifier.offset((85).dp,(-20).dp))
            }
            Row{
                Text("Rain (mm/h) ", fontWeight = FontWeight.Bold, color = GrayL,
                    modifier = modifier.offset((-45).dp))
                Text("Wind (km/h)", fontWeight = FontWeight.Bold, color = GrayL,
                    modifier = modifier.offset(65.dp))
            }
            Row{
                Text(
                    viewModel.chuvaSearch.value, fontWeight = FontWeight.Bold,
                    modifier = modifier.offset((-50).dp,20.dp))
                Text(
                    viewModel.ventoSearch.value, fontWeight = FontWeight.Bold,
                    modifier = modifier.offset((75).dp,20.dp))
            }
            Text("Uv",  fontWeight = FontWeight.Bold, color = GrayL,
                modifier = modifier.offset((0).dp,40.dp))
            Text(
                viewModel.uvSearch.value,  fontWeight = FontWeight.Bold,
                modifier = modifier.offset((0).dp,45.dp))
        }


    }


}