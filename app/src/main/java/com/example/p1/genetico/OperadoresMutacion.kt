package com.example.p1.genetico

import kotlin.random.Random

object OperadoresMutacion {

    fun mutacionIntercambio(individuo: Individuo, prob: Float) {
        for (i in individuo.orden.indices) {
            if (Random.Default.nextDouble() < prob) {
                val j = Random.Default.nextInt(individuo.orden.size)
                intercambiar(individuo.orden, i, j)
            }
        }
    }

    fun mutacionInversion(individuo: Individuo, prob: Double) {
        if (Random.Default.nextDouble() < prob) {
            val indices = (0 until individuo.orden.size).shuffled().take(2).sorted()
            val inicio = indices[0]
            val fin = indices[1]

            individuo.orden.subList(inicio, fin + 1).reverse()
        }
    }

    fun mutacion2Opt(individuo: Individuo, prob: Double) {
        if (Random.Default.nextDouble() < prob) {
            val i = Random.Default.nextInt(individuo.orden.size - 1)
            val j = Random.Default.nextInt(i + 1, individuo.orden.size)

            // Invertir el segmento entre i y j
            val segmento = individuo.orden.subList(i, j + 1)
            segmento.reverse()
        }
    }

    private fun intercambiar(lista: MutableList<Int>, i: Int, j: Int) {
        val temp = lista[i]
        lista[i] = lista[j]
        lista[j] = temp
    }
}