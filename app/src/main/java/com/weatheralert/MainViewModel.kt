package com.weatheralert

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.weatheralert.api.WeatherApiResponse
import com.weatheralert.api.WeatherServiceAPI
import com.weatheralert.ui.nav.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainViewModel : ViewModel() {

    var page = mutableStateOf(Route.Home.route)
    val temperatura = mutableStateOf("--")
    val umidade = mutableStateOf("--")
    val chuva = mutableStateOf("--")
    val vento = mutableStateOf("--")
    val uv = mutableStateOf("--")
    val cidade = mutableStateOf("Loading...")
    private val weatherService: WeatherServiceAPI by lazy {
        Retrofit.Builder()
            .baseUrl(WeatherServiceAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherServiceAPI::class.java)
    }

    fun getYourLocation(lat: Double, lon: Double) {
        val location = "$lat,$lon"
        val call = weatherService.getWeatherForecast(location)
        call.enqueue(object : retrofit2.Callback<WeatherApiResponse> {
            override fun onResponse(
                call: retrofit2.Call<WeatherApiResponse>,
                response: retrofit2.Response<WeatherApiResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    cidade.value = data?.location?.name ?: "Unknow"
                    temperatura.value = data?.forecast?.forecastday?.get(0)?.day?.avgtemp_c.toString()
                    umidade.value = data?.forecast?.forecastday?.get(0)?.day?.avghumidity.toString()
                    chuva.value = data?.forecast?.forecastday?.get(0)?.day?.totalprecip_mm.toString()
                    vento.value = data?.forecast?.forecastday?.get(0)?.day?.maxwind_kph.toString()
                    uv.value = data?.forecast?.forecastday?.get(0)?.day?.uv.toString()
                } else {
                    Log.e("Weather", "Erro na API: ${response.code()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<WeatherApiResponse>, t: Throwable) {
                Log.e("Weather", "Falha na requisição: ${t.message}")
            }
        })
    }
}
