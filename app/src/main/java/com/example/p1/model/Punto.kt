package com.example.p1.model

data class Punto(
    val id: Long,
    val rutaId: Long,
    val x: Int,
    val y: Int,
    val controlX: Float?,
    val controlY: Float?
)
