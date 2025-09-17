package com.example.p1.genetico

import com.example.p1.CalculadorDistancia
import com.example.p1.genetico.Individuo
import com.example.p1.Punto
import kotlin.random.Random

object GeneradorPoblacion {

    fun generarPoblacionAleatoria(numCiudades: Int, tamPoblacion: Int): List<Individuo> {
        val poblacion = mutableListOf<Individuo>()
        repeat(tamPoblacion) {
            val orden = (0 until numCiudades).toMutableList()
            orden.shuffle(Random.Default)
            poblacion.add(Individuo(orden))
        }
        return poblacion
    }

    fun generarPoblacionGreedy(numCiudades: Int, tamPoblacion: Int, puntos: List<Punto>): List<Individuo> {
        val poblacion = mutableListOf<Individuo>()

        // Generar algunos individuos con estrategia greedy
        val numGreedy = tamPoblacion / 4
        repeat(numGreedy) {
            val orden = generarRutaGreedy(numCiudades, puntos)
            poblacion.add(Individuo(orden))
        }

        // Completar con individuos aleatorios
        while (poblacion.size < tamPoblacion) {
            val orden = (0 until numCiudades).toMutableList()
            orden.shuffle(Random.Default)
            poblacion.add(Individuo(orden))
        }

        return poblacion
    }

    private fun generarRutaGreedy(numCiudades: Int, puntos: List<Punto>): MutableList<Int> {
        val visitados = mutableSetOf<Int>()
        val ruta = mutableListOf<Int>()

        var ciudadActual = Random.Default.nextInt(numCiudades)
        ruta.add(ciudadActual)
        visitados.add(ciudadActual)

        while (visitados.size < numCiudades) {
            var mejorCiudad = -1
            var mejorDistancia = Double.MAX_VALUE

            for (ciudad in 0 until numCiudades) {
                if (ciudad !in visitados) {
                    val distancia = CalculadorDistancia.calcularDistanciaEuclidiana(
                        puntos[ciudadActual], puntos[ciudad]
                    )
                    if (distancia < mejorDistancia) {
                        mejorDistancia = distancia
                        mejorCiudad = ciudad
                    }
                }
            }

            if (mejorCiudad != -1) {
                ruta.add(mejorCiudad)
                visitados.add(mejorCiudad)
                ciudadActual = mejorCiudad
            }
        }

        return ruta
    }
}