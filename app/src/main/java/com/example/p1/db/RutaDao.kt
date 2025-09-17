package com.example.p1.db

import androidx.room.*

@Dao
interface RutaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuta(ruta: Ruta): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuntos(puntos: List<PuntoEntity>)

    @Transaction
    @Query("SELECT * FROM rutas")
    suspend fun getRutasConPuntos(): List<RutaConPuntos>

    @Transaction
    @Query("SELECT * FROM rutas WHERE id = :rutaId")
    suspend fun getRutaConPuntosById(rutaId: Int): RutaConPuntos?
}
