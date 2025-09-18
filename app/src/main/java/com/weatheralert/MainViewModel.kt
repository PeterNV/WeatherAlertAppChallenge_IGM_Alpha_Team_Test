package com.weatheralert

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

import com.weatheralert.api.WeatherApiResponse
import com.weatheralert.api.WeatherServiceAPI
import com.weatheralert.ui.nav.Route
import retrofit2.Call
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
                call: Call<WeatherApiResponse>,
                response: Response<WeatherApiResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    cidade.value = data?.location?.name ?: "Unknow"
                    temperatura.value = data?.forecast?.forecastday?.get(0)?.day?.avgtemp_c.toString()
                    umidade.value = data?.forecast?.forecastday?.get(0)?.day?.avghumidity.toString()
                    chuva.value = data?.forecast?.forecastday?.get(0)?.day?.totalprecip_mm.toString()
                    vento.value = data?.forecast?.forecastday?.get(0)?.day?.maxwind_kph.toString()
                    uv.value = data?.forecast?.forecastday?.get(0)?.day?.uv.toString()
                    iconeClima.value = "https:${data?.current?.condition?.icon ?: ""}"
                } else {
                    Log.e("Weather", "Erro na API: ${response.code()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<WeatherApiResponse>, t: Throwable) {
                Log.e("Weather", "Falha na requisição: ${t.message}")
            }
        })
    }

    fun getCity(City: String) {
        val location = City
        val call = weatherService.getWeatherForecast(location)
        call.enqueue(object : retrofit2.Callback<WeatherApiResponse> {
            override fun onResponse(
                call: retrofit2.Call<WeatherApiResponse>,
                response: retrofit2.Response<WeatherApiResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    cidadeSearch.value = data?.location?.name ?: "Unknow"
                    temperaturaSearch.value = data?.forecast?.forecastday?.get(0)?.day?.avgtemp_c.toString()
                    umidadeSearch.value = data?.forecast?.forecastday?.get(0)?.day?.avghumidity.toString()
                    chuvaSearch.value = data?.forecast?.forecastday?.get(0)?.day?.totalprecip_mm.toString()
                    ventoSearch.value = data?.forecast?.forecastday?.get(0)?.day?.maxwind_kph.toString()
                    uvSearch.value = data?.forecast?.forecastday?.get(0)?.day?.uv.toString()
                } else {
                    Log.e("Weather", "Erro na API: ${response.code()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<WeatherApiResponse>, t: Throwable) {
                Log.e("Weather", "Falha na requisição: ${t.message}")
            }
        })
    }

    fun getCityMap(lat: Double, lon: Double) {
        val location = "$lat,$lon"
        val call = weatherService.getWeatherForecast(location)
        call.enqueue(object : retrofit2.Callback<WeatherApiResponse> {
            override fun onResponse(
                call: retrofit2.Call<WeatherApiResponse>,
                response: retrofit2.Response<WeatherApiResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    cidadeMap.value = data?.location?.name ?: "Unknow"
                    temperaturaMap.value = data?.forecast?.forecastday?.get(0)?.day?.avgtemp_c.toString()
                    umidadeMap.value = data?.forecast?.forecastday?.get(0)?.day?.avghumidity.toString()
                    chuvaMap.value = data?.forecast?.forecastday?.get(0)?.day?.totalprecip_mm.toString()
                    ventoMap.value = data?.forecast?.forecastday?.get(0)?.day?.maxwind_kph.toString()
                    uvMap.value = data?.forecast?.forecastday?.get(0)?.day?.uv.toString()
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
