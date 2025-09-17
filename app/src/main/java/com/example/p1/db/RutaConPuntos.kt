package com.example.p1.db

import androidx.room.Embedded
import androidx.room.Relation

data class RutaConPuntos(
    @Embedded val ruta: Ruta,
    @Relation(
        parentColumn = "id",
        entityColumn = "rutaId"
    )
    val puntos: List<PuntoEntity>
)
