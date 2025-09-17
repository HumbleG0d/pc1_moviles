package com.example.p1

import kotlin.math.pow
import kotlin.math.sqrt

object CalculadorDistancia {

    fun calcularDistanciaEuclidiana(p1: Punto, p2: Punto): Double {
        val dx = (p1.x - p2.x).toDouble()
        val dy = (p1.y - p2.y).toDouble()
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    fun calcularDistanciaRuta(ruta: List<Int>, puntos: List<Punto>): Double {
        var distanciaTotal = 0.0
        for (i in 0 until ruta.size - 1) {
            val p1 = puntos[ruta[i]]
            val p2 = puntos[ruta[i + 1]]
            distanciaTotal += calcularDistanciaEuclidiana(p1, p2)
        }
        return distanciaTotal
    }
}