package com.weatheralert.api

data class WeatherApiResponse(
    val location: LocationData?,
    val current: CurrentWeather?,
    val forecast: Forecast?,
    val season: Season?
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

data class Forecast(
    val forecastday: List<ForecastDay>?
)
data class Season(
    val seasonMonth: List<Season>?
)
data class ForecastDay(
    val date: String?,
    val day: DayData?
)

data class DayData(
    val avgtemp_c: Double?,
    val avghumidity: Double?,
    val totalprecip_mm: Double?,
    val maxwind_kph: Double?,
    val uv: Double?
)
