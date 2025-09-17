package com.example.p1.genetico

import kotlin.random.Random

object SelectorPadres {

    fun seleccionTorneo(poblacion: List<Individuo>, tamanioTorneo: Int = 3): Individuo {
        val participantes = poblacion.shuffled().take(tamanioTorneo)
        return participantes.minByOrNull { it.distancia } ?: participantes.first()
    }

    fun seleccionRuleta(poblacion: List<Individuo>): Individuo {
        val maxDistancia = poblacion.maxOfOrNull { it.distancia } ?: 0.0
        val fitness = poblacion.map { maxDistancia - it.distancia + 1 }
        val totalFitness = fitness.sum()

        var random = Random.Default.nextDouble() * totalFitness
        for (i in poblacion.indices) {
            random -= fitness[i]
            if (random <= 0) {
                return poblacion[i]
            }
        }
        return poblacion.last()
    }

    fun seleccionElitista(poblacion: List<Individuo>, porcentaje: Double): List<Individuo> {
        val numElites = (poblacion.size * porcentaje).toInt()
        return poblacion.sortedBy { it.distancia }.take(numElites)
    }
}