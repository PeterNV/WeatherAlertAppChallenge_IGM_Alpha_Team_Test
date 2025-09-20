package com.weatheralert.repo

// Repository.kt
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weatheralert.model.City
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Repository(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("weather_favorites", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val favoriteCitiesKey = "favorite_cities"

    val favoriteCities: Flow<List<City>> = flow {
        val cities = getFavoriteCities()
        emit(cities)
    }

    suspend fun addFavoriteCity(city: City) {
        val currentFavorites = getFavoriteCities().toMutableList()
        // Remove se já existir para evitar duplicatas
        currentFavorites.removeAll { it.name == city.name }
        currentFavorites.add(city)
        saveFavoriteCities(currentFavorites)
    }

    suspend fun removeFavoriteCity(city: City) {
        val currentFavorites = getFavoriteCities().toMutableList()
        currentFavorites.removeAll { it.name == city.name }
        saveFavoriteCities(currentFavorites)
    }

    suspend fun isCityFavorite(cityName: String): Boolean {
        return getFavoriteCities().any { it.name == cityName }
    }

    private fun getFavoriteCities(): List<City> {
        val json = sharedPreferences.getString(favoriteCitiesKey, "[]") ?: "[]"
        val type = object : TypeToken<List<City>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveFavoriteCities(cities: List<City>) {
        val json = gson.toJson(cities)
        sharedPreferences.edit().putString(favoriteCitiesKey, json).apply()
    }
}