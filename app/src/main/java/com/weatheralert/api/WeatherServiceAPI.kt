

package com.weatheralert.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherServiceAPI {
    companion object {
        const val BASE_URL = "https://api.weatherapi.com/v1/"
        const val API_KEY = "057cfec1751240379e4212020251209"
    }

    @GET("forecast.json")
    fun getWeatherForecast(
        @Query("q") location: String,  // pode ser "latitude,longitude"
        @Query("days") days: Int = 5,  // próximos 5 dias
        @Query("aqi") aqi: String = "no",
        @Query("alerts") alerts: String = "no",
        @Query("key") apiKey: String = API_KEY
    ): Call<WeatherApiResponse>
}
