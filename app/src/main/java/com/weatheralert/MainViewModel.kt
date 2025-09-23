package com.weatheralert

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.weatheralert.api.WeatherApiResponse
import com.weatheralert.api.WeatherServiceAPI
import com.weatheralert.ui.nav.Route
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainViewModel() : ViewModel() {

    var page = mutableStateOf(Route.Home.route)
    val temperatura = mutableStateOf("--")
    val umidade = mutableStateOf("--")
    val chuva = mutableStateOf("--")
    val vento = mutableStateOf("--")
    val uv = mutableStateOf("--")
    val cidade = mutableStateOf("Loading...")

    val temperaturaSearch = mutableStateOf("--")
    val umidadeSearch = mutableStateOf("--")
    val chuvaSearch = mutableStateOf("--")
    val ventoSearch = mutableStateOf("--")
    val uvSearch = mutableStateOf("--")
    val cidadeSearch = mutableStateOf("...")

    val temperaturaMap = mutableStateOf("--")
    val umidadeMap = mutableStateOf("--")
    val chuvaMap = mutableStateOf("--")
    val ventoMap = mutableStateOf("--")
    val uvMap = mutableStateOf("--")
    val cidadeMap= mutableStateOf("...")
    val iconeClima = mutableStateOf("")
    val iconeClimaMap = mutableStateOf("")
    val iconeClimaSearch = mutableStateOf("")

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
        call.enqueue(object : Callback<WeatherApiResponse> {
            override fun onResponse(
                call: Call<WeatherApiResponse>,
                response: Response<WeatherApiResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    cidade.value = data?.location?.name ?: "Unknow"
                    // DADOS ATUAIS - CORRIGIDOS
                    temperatura.value = data?.current?.temp_c?.toString() ?: "--"
                    umidade.value = data?.current?.humidity?.toString() ?: "--"
                    chuva.value = data?.current?.precip_mm?.toString() ?: "--"
                    vento.value = data?.current?.wind_kph?.toString() ?: "--"
                    uv.value = data?.current?.uv?.toString() ?: "--"
                    iconeClima.value = "https:${data?.current?.condition?.icon ?: ""}"
                } else {
                    Log.e("Weather", "Erro na API: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WeatherApiResponse>, t: Throwable) {
                Log.e("Weather", "Falha na requisição: ${t.message}")
            }
        })

    }

    fun getCity(City: String) {
        val location = City
        val call = weatherService.getWeatherForecast(location)
        call.enqueue(object : Callback<WeatherApiResponse> {
            override fun onResponse(
                call: Call<WeatherApiResponse>,
                response: Response<WeatherApiResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    cidadeSearch.value = data?.location?.name ?: "Unknow"
                    // DADOS ATUAIS - CORRIGIDOS
                    temperaturaSearch.value = data?.current?.temp_c?.toString() ?: "--"
                    umidadeSearch.value = data?.current?.humidity?.toString() ?: "--"
                    chuvaSearch.value = data?.current?.precip_mm?.toString() ?: "--"
                    ventoSearch.value = data?.current?.wind_kph?.toString() ?: "--"
                    uvSearch.value = data?.current?.uv?.toString() ?: "--"
                    iconeClimaSearch.value = "https:${data?.current?.condition?.icon ?: ""}"
                } else {
                    Log.e("Weather", "Erro na API: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WeatherApiResponse>, t: Throwable) {
                Log.e("Weather", "Falha na requisição: ${t.message}")
            }
        })
    }

    fun getCityMap(lat: Double, lon: Double) {
        val location = "$lat,$lon"
        val call = weatherService.getWeatherForecast(location)
        call.enqueue(object : Callback<WeatherApiResponse> {
            override fun onResponse(
                call: Call<WeatherApiResponse>,
                response: Response<WeatherApiResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    cidadeMap.value = data?.location?.name ?: "Unknow"
                    // DADOS ATUAIS - CORRIGIDOS
                    temperaturaMap.value = data?.current?.temp_c?.toString() ?: "--"
                    umidadeMap.value = data?.current?.humidity?.toString() ?: "--"
                    chuvaMap.value = data?.current?.precip_mm?.toString() ?: "--"
                    ventoMap.value = data?.current?.wind_kph?.toString() ?: "--"
                    uvMap.value = data?.current?.uv?.toString() ?: "--"
                    iconeClimaMap.value = "https:${data?.current?.condition?.icon ?: ""}"
                } else {
                    Log.e("Weather", "Erro na API: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WeatherApiResponse>, t: Throwable) {
                Log.e("Weather", "Falha na requisição: ${t.message}")
            }
        })
    }
}

fun countBrokenLines(text: String): Int {
    return text.count { it == '\n' }
}