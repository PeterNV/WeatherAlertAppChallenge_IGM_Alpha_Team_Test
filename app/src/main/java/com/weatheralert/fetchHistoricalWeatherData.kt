package com.weatheralert

import android.content.Context
import android.location.Geocoder
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

            // Agora buscar dados da NASA POWER API com as coordenadas
            val today = LocalDate.now()
            val sevenDaysAgo = today.minusDays(7)
            val startDate = sevenDaysAgo.format(DateTimeFormatter.BASIC_ISO_DATE)
            val endDate = today.format(DateTimeFormatter.BASIC_ISO_DATE)
            val params = "T2M,T2M_MAX,T2M_MIN,RH2M,RH2M_MAX,RH2M_MIN,PRECTOTCORR,WS10M,WS10M_MAX,WD10M,PS,QV2M,GWETTOP,GWETROOT,ALLSKY_SFC_SW_DWN,ALLSKY_SFC_LW_DWN,CLRSKY_SFC_SW_DWN,ALLSKY_KT,CDD0,HDD18,LTS,LPT"

            val powerUrl = "https://power.larc.nasa.gov/api/temporal/daily/point?" +
                    "parameters=$params&community=RE&longitude=$lon&latitude=$lat" +
                    "&start=$startDate&end=$endDate&format=JSON"

            // Executar em uma thread em background
            Thread {
                try {
                    val connection = URL(powerUrl).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val jsonData = JSONObject(response)

                        if (jsonData.has("properties")) {
                            val properties = jsonData.getJSONObject("properties")
                            val parameter = properties.getJSONObject("parameter")

                            // Processar dados para criar um resumo
                            val summary = StringBuilder()

                            // Temperatura média (T2M)
                            val temperatures = parameter.getJSONObject("T2M")
                            var tempSum = 0.0
                            var tempCount = 0
                            for (key in temperatures.keys()) {
                                tempSum += temperatures.getDouble(key)
                                tempCount++
                            }
                            val avgTemp = tempSum / tempCount
                            summary.append("Temperatura média: ${"%.1f".format(avgTemp)}°C. ")

                            // Umidade (RH2M)
                            val humidity = parameter.getJSONObject("RH2M")
                            var humiditySum = 0.0
                            var humidityCount = 0
                            for (key in humidity.keys()) {
                                humiditySum += humidity.getDouble(key)
                                humidityCount++
                            }
                            val avgHumidity = humiditySum / humidityCount
                            summary.append("Umidade média: ${"%.1f".format(avgHumidity)}%. ")

                            // Precipitação (PRECTOTCORR)
                            val precipitation = parameter.getJSONObject("PRECTOTCORR")
                            var precipSum = 0.0
                            for (key in precipitation.keys()) {
                                precipSum += precipitation.getDouble(key)
                            }
                            summary.append("Precipitação total: ${"%.1f".format(precipSum)}mm. ")

                            // Velocidade do vento (WS10M)
                            val windSpeed = parameter.getJSONObject("WS10M")
                            var windSum = 0.0
                            var windCount = 0
                            for (key in windSpeed.keys()) {
                                windSum += windSpeed.getDouble(key)
                                windCount++
                            }
                            val avgWindSpeed = windSum / windCount
                            summary.append("Velocidade média do vento: ${"%.1f".format(avgWindSpeed)}m/s.")

                            callback(summary.toString())
                        } else {
                            callback("Dados históricos não disponíveis para esta localização.")
                        }
                    } else {
                        callback("Erro ao buscar dados históricos: Código $responseCode")
                    }
                } catch (e: Exception) {
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