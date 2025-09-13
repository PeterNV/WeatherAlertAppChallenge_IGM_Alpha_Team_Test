package com.weatheralert.api

data class WeatherApiResponse(
    val location: Location,
    val forecast: Forecast
)

data class Location(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double
)

data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val day: Day
)

data class Day(
    val avgtemp_c: Double,
    val avghumidity: Double,
    val totalprecip_mm: Double,
    val maxwind_kph: Double,
    val uv: Double
)
