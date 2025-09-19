package com.weatheralert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.weatheralert.ui.theme.Black
import com.weatheralert.ui.theme.White

//@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritePage(modifier: Modifier = Modifier, viewModel: MainViewModel){

    Column (horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, modifier = modifier.background(color = White).fillMaxSize()) {
            Text("No favorite", color = Black, fontSize = 18.sp)
    }

}