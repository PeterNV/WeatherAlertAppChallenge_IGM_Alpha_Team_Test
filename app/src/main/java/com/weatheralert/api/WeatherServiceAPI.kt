

package com.weatheralert.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.weatheralert.BuildConfig
interface WeatherServiceAPI {
    companion object {
        const val BASE_URL = "https://api.weatherapi.com/v1/"
        const val API_KEY = BuildConfig.WEATHER_API_KEY
    }

    @GET("forecast.json")
    fun getWeatherForecast(
        @Query("q") location: String,  // pode ser "latitude,longitude"

        @Query("key") apiKey: String = API_KEY
    ): Call<WeatherApiResponse>

}
