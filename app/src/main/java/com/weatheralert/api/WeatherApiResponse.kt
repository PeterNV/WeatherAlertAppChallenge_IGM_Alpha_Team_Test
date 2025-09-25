package com.weatheralert.api

data class WeatherApiResponse(
    val location: LocationData?,
    val current: CurrentWeather?
)

data class LocationData(
    val name: String?,
    val region: String?,
    val country: String?
)

data class CurrentWeather(
    val temp_c: Double?,
    val humidity: Double?,
    val precip_mm: Double?,
    val wind_kph: Double?,
    val uv: Double?,
    val condition: Conditions?
)

data class Conditions(
    val text: String?,
    val icon: String?
)





