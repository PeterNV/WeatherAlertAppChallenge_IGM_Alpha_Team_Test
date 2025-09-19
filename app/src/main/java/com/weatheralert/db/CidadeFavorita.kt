package com.weatheralert.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cidades_favoritas")
data class CidadeFavorita(
    @PrimaryKey val nome: String,
    val latitude: Double,
    val longitude: Double
)