package com.example.p1.genetico

import com.example.p1.CalculadorDistancia
import com.example.p1.Punto

class Individuo(val orden: MutableList<Int>) {
    var distancia: Double = 0.0

    fun calcularDistancia(puntos: List<Punto>): Double {
        distancia = 0.0
        for (i in 0 until orden.size - 1) {
            val punto1 = puntos[orden[i]]
            val punto2 = puntos[orden[i + 1]]
            distancia += CalculadorDistancia.calcularDistanciaEuclidiana(punto1, punto2)
        }
        return distancia
    }

    fun clonar(): Individuo {
        return Individuo(orden.toMutableList())
    }

    override fun toString(): String {
        return "Individuo(orden=$orden, distancia=$distancia)"
    }
}