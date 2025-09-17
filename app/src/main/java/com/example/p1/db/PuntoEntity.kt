package com.example.p1.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "puntos",
    foreignKeys = [ForeignKey(
        entity = Ruta::class,
        parentColumns = ["id"],
        childColumns = ["rutaId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PuntoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val rutaId: Int,
    val x: Int,
    val y: Int
)
