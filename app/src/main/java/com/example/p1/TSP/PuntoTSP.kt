package com.example.p1.TSP

import kotlin.math.sqrt

data class PuntoTSP(val x: Int, val y: Int)

class IndividuoTSP(val orden: MutableList<Int>) {
    var distancia: Double = 0.0

    fun calcularDistancia(puntos: List<PuntoTSP>): Double {
        distancia = 0.0
        for (i in 0 until orden.size - 1) {
            val p1 = puntos[orden[i]]
            val p2 = puntos[orden[i + 1]]
            val dx = (p1.x - p2.x).toDouble()
            val dy = (p1.y - p2.y).toDouble()
            distancia += sqrt(dx * dx + dy * dy)
        }
        return distancia
    }
}
