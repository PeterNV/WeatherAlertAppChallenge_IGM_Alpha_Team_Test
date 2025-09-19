package com.weatheralert.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CidadeFavorita::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cidadeFavoritaDao(): CidadeFavoritaDao
}