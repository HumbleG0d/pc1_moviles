package com.example.p1.TSP

import com.example.p1.Punto

data class ConfiguracionTSP(
    val numCiudades: Int,
    val tamPoblacion: Int,
    val probMutacion: Float,
    val generaciones: Int,
    val coordenadas: List<Punto>
)