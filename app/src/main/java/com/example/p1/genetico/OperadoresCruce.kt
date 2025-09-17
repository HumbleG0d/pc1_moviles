package com.example.p1.genetico

import kotlin.random.Random

object OperadoresCruce {

    fun cruceOX(padre1: Individuo, padre2: Individuo): Individuo {
        val indices = (0 until padre1.orden.size).shuffled().take(2).sorted()
        val a = indices[0]
        val b = indices[1]

        val intermedio = padre1.orden.subList(a, b).toMutableList()
        val resto = padre2.orden.filter { it !in intermedio }.toMutableList()

        val hijo = mutableListOf<Int>()
        hijo.addAll(resto.take(a))
        hijo.addAll(intermedio)
        hijo.addAll(resto.drop(a))

        return Individuo(hijo)
    }

    fun crucePMX(padre1: Individuo, padre2: Individuo): Individuo {
        val size = padre1.orden.size
        val inicio = Random.Default.nextInt(size)
        val fin = Random.Default.nextInt(inicio, size)

        val hijo = MutableList(size) { -1 }
        val mapeo = mutableMapOf<Int, Int>()

        // Copiar segmento del padre1
        for (i in inicio..fin) {
            hijo[i] = padre1.orden[i]
            mapeo[padre2.orden[i]] = padre1.orden[i]
        }

        // Completar con genes del padre2
        for (i in 0 until size) {
            if (hijo[i] == -1) {
                var gen = padre2.orden[i]
                while (gen in mapeo.values) {
                    gen = mapeo[gen] ?: gen
                }
                hijo[i] = gen
            }
        }

        return Individuo(hijo)
    }
}