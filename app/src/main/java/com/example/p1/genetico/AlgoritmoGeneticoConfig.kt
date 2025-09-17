package com.example.p1.genetico

object AlgoritmoGeneticoConfig {
    data class Configuracion(
        val numCiudades: Int,
        val tamPoblacion: Int,
        val probMutacion: Double,
        val generaciones: Int,
        val coordenadas: List<Int>
    )

    fun crearConfiguracion(numPuntos: Int, coordenadas: List<Int>): Configuracion {
        return Configuracion(
            numCiudades = numPuntos,
            tamPoblacion = when {
                numPuntos <= 5 -> 100
                numPuntos <= 10 -> 200
                numPuntos <= 15 -> 300
                else -> 500
            },
            probMutacion = when {
                numPuntos <= 5 -> 0.1
                numPuntos <= 10 -> 0.15
                else -> 0.2
            },
            generaciones = when {
                numPuntos <= 5 -> 150
                numPuntos <= 10 -> 300
                numPuntos <= 15 -> 500
                else -> 1000
            },
            coordenadas = coordenadas
        )
    }
}