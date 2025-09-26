package com.weatheralert

import android.content.Context
import android.location.Geocoder
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun fetchHistoricalWeatherData(cityName: String, context: Context, callback: (String) -> Unit) {
    // Primeiro, precisamos geocodificar o nome da cidade para coordenadas
    val geocoder = Geocoder(context, Locale.getDefault())

    try {
        val addresses = geocoder.getFromLocationName(cityName, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            val lat = address.latitude
            val lon = address.longitude

            // Verificar se as coordenadas são válidas
            if (lat == 0.0 && lon == 0.0) {
                callback("Coordenadas inválidas para: $cityName")
                return
            }

            // Agora buscar dados da NASA POWER API com as coordenadas
            val today = LocalDate.now()
            val nineDaysAgo = today.minusDays(9)

            // Formatar datas corretamente para a API NASA (YYYYMMDD)
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            val startDate = nineDaysAgo.format(formatter)
            val endDate = today.format(formatter)

            // Parâmetros simplificados para evitar erro 422
            val params = "T2M,RH2M,PRECTOTCORR,WS10M"

            val powerUrl = "https://power.larc.nasa.gov/api/temporal/daily/point?" +
                    "parameters=$params&community=RE&longitude=$lon&latitude=$lat" +
                    "&start=$startDate&end=$endDate&format=JSON"

            Log.d("NASA_API", "URL: $powerUrl")

            // Executar em uma thread em background
            Thread {
                try {
                    val url = URL(powerUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000
                    connection.setRequestProperty("User-Agent", "WeatherApp/1.0")

                    val responseCode = connection.responseCode
                    Log.d("NASA_API", "Response Code: $responseCode")

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        Log.d("NASA_API", "Response: ${response.take(500)}") // Log parcial para debug

                        val jsonData = JSONObject(response)

                        if (jsonData.has("properties")) {
                            val properties = jsonData.getJSONObject("properties")
                            val parameter = properties.getJSONObject("parameter")

                            // Processar dados para criar um resumo
                            val summary = StringBuilder()

                            // Função auxiliar para filtrar valores válidos
                            fun isValidValue(value: Double, parameterName: String): Boolean {
                                return when (parameterName) {
                                    "T2M" -> value > -100.0 && value < 60.0 // Temperatura entre -100°C e 60°C
                                    "RH2M" -> value >= 0.0 && value <= 100.0 // Umidade entre 0% e 100%
                                    "PRECTOTCORR" -> value >= 0.0 && value < 1000.0 // Precipitação não negativa e menor que 1000mm
                                    "WS10M" -> value >= 0.0 && value < 150.0 // Velocidade do vento não negativa e menor que 150m/s
                                    else -> value != -999.0 && value > -1000.0 && value < 1000.0 // Filtro genérico
                                }
                            }

                            // Temperatura média (T2M)
                            try {
                                val temperatures = parameter.getJSONObject("T2M")
                                var tempSum = 0.0
                                var tempCount = 0
                                for (key in temperatures.keys()) {
                                    val value = temperatures.getDouble(key)
                                    if (isValidValue(value, "T2M")) {
                                        tempSum += value
                                        tempCount++
                                    } else {
                                        Log.d("NASA_API", "Valor irreais de temperatura filtrado: $value")
                                    }
                                }
                                val avgTemp = if (tempCount > 0) tempSum / tempCount else 0.0
                                summary.append("Temperatura média: ${"%.1f".format(avgTemp)}°C. ")
                            } catch (e: Exception) {
                                summary.append("Temperatura: dados indisponíveis. ")
                            }

                            // Umidade (RH2M)
                            try {
                                val humidity = parameter.getJSONObject("RH2M")
                                var humiditySum = 0.0
                                var humidityCount = 0
                                for (key in humidity.keys()) {
                                    val value = humidity.getDouble(key)
                                    if (isValidValue(value, "RH2M")) {
                                        humiditySum += value
                                        humidityCount++
                                    } else {
                                        Log.d("NASA_API", "Valor irreais de umidade filtrado: $value")
                                    }
                                }
                                val avgHumidity = if (humidityCount > 0) humiditySum / humidityCount else 0.0
                                summary.append("Umidade média: ${"%.1f".format(avgHumidity)}%. ")
                            } catch (e: Exception) {
                                summary.append("Umidade: dados indisponíveis. ")
                            }

                            // Precipitação (PRECTOTCORR)
                            try {
                                val precipitation = parameter.getJSONObject("PRECTOTCORR")
                                var precipSum = 0.0
                                var precipCount = 0
                                for (key in precipitation.keys()) {
                                    val value = precipitation.getDouble(key)
                                    if (isValidValue(value, "PRECTOTCORR")) {
                                        precipSum += value
                                        precipCount++
                                    } else {
                                        Log.d("NASA_API", "Valor irreais de precipitação filtrado: $value")
                                    }
                                }
                                val avgPrecip = if (precipCount > 0) precipSum else 0.0
                                summary.append("Precipitação total: ${"%.1f".format(avgPrecip)}mm. ")
                            } catch (e: Exception) {
                                summary.append("Precipitação: dados indisponíveis. ")
                            }

                            // Velocidade do vento (WS10M)
                            try {
                                val windSpeed = parameter.getJSONObject("WS10M")
                                var windSum = 0.0
                                var windCount = 0
                                for (key in windSpeed.keys()) {
                                    val value = windSpeed.getDouble(key)
                                    if (isValidValue(value, "WS10M")) {
                                        windSum += value
                                        windCount++
                                    } else {
                                        Log.d("NASA_API", "Valor irreais de vento filtrado: $value")
                                    }
                                }
                                val avgWindSpeed = if (windCount > 0) windSum / windCount else 0.0
                                summary.append("Velocidade média do vento: ${"%.1f".format(avgWindSpeed)}m/s.")
                            } catch (e: Exception) {
                                summary.append("Vento: dados indisponíveis.")
                            }

                            callback(summary.toString())
                        } else {
                            callback("Dados históricos não disponíveis para esta localização.")
                        }
                    } else {
                        // Ler mensagem de erro se houver
                        val errorStream = connection.errorStream
                        val errorMessage = if (errorStream != null) {
                            errorStream.bufferedReader().use { it.readText() }
                        } else {
                            "Erro HTTP: $responseCode"
                        }
                        callback("Erro ao buscar dados históricos: $errorMessage")
                    }
                } catch (e: Exception) {
                    Log.e("NASA_API", "Exception: ${e.message}", e)
                    callback("Erro ao processar dados históricos: ${e.message}")
                }
            }.start()
        } else {
            callback("Localização não encontrada: $cityName")
        }
    } catch (e: Exception) {
        callback("Erro ao geocodificar cidade: ${e.message}")
    }
}