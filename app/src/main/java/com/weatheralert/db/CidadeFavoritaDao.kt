package com.weatheralert.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CidadeFavoritaDao {
    @Insert
    suspend fun inserir(cidade: CidadeFavorita)

    @Delete
    suspend fun deletar(cidade: CidadeFavorita)

    @Query("SELECT * FROM cidades_favoritas ORDER BY nome")
    fun obterTodas(): Flow<List<CidadeFavorita>>

    @Query("SELECT * FROM cidades_favoritas WHERE nome = :nomeCidade")
    suspend fun buscarPorNome(nomeCidade: String): CidadeFavorita?
}