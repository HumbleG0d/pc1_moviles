package com.example.p1.TSP

import com.example.p1.genetico.AlgoritmoGeneticoConfig
import kotlin.math.sqrt
import kotlin.random.Random

object TSPSolverModular {
    fun resolverTSP(config: AlgoritmoGeneticoConfig.Configuracion): List<Int> {
        // Convertir coordenadas a puntos
        val puntos = mutableListOf<PuntoTSP>()
        for (i in config.coordenadas.indices step 2) {
            if (i + 1 < config.coordenadas.size) {
                puntos.add(PuntoTSP(config.coordenadas[i], config.coordenadas[i + 1]))
            }
        }

        // Generar población inicial con estrategia híbrida
        var poblacion = generarPoblacionHibrida(config.numCiudades, config.tamPoblacion, puntos)

        // Evaluar población inicial
        poblacion.forEach { it.calcularDistancia(puntos) }

        // Evolución con élites y diversidad
        repeat(config.generaciones) {
            poblacion.sortBy { it.distancia }

            // Mantener 20% de élites
            val elites = poblacion.take((config.tamPoblacion * 0.2).toInt())
            val nuevaGen = elites.map { IndividuoTSP(it.orden.toMutableList()) }.toMutableList()

            // Generar nueva descendencia
            while (nuevaGen.size < config.tamPoblacion) {
                val padre1 = seleccionTorneo(poblacion, 3)
                val padre2 = seleccionTorneo(poblacion, 3)

                val hijo = cruzarOX(padre1, padre2)

                // Aplicar múltiples tipos de mutación
                mutarIntercambio(hijo, config.probMutacion)
                if (Random.nextDouble() < 0.3) {
                    mutar2Opt(hijo, 0.5)
                }

                hijo.calcularDistancia(puntos)
                nuevaGen.add(hijo)
            }

            poblacion = nuevaGen
        }

        val mejor = poblacion.minByOrNull { it.distancia } ?: poblacion.first()
        return mejor.orden
    }

    private fun generarPoblacionHibrida(numCiudades: Int, tamPoblacion: Int, puntos: List<PuntoTSP>): MutableList<IndividuoTSP> {
        val poblacion = mutableListOf<IndividuoTSP>()

        // 30% población greedy
        val numGreedy = (tamPoblacion * 0.3).toInt()
        repeat(numGreedy) {
            val rutaGreedy = generarRutaGreedy(numCiudades, puntos)
            poblacion.add(IndividuoTSP(rutaGreedy))
        }

        // 70% población aleatoria
        while (poblacion.size < tamPoblacion) {
            val orden = (0 until numCiudades).shuffled().toMutableList()
            poblacion.add(IndividuoTSP(orden))
        }

        return poblacion
    }

    private fun generarRutaGreedy(numCiudades: Int, puntos: List<PuntoTSP>): MutableList<Int> {
        val visitados = mutableSetOf<Int>()
        val ruta = mutableListOf<Int>()

        var ciudadActual = Random.nextInt(numCiudades)
        ruta.add(ciudadActual)
        visitados.add(ciudadActual)

        while (visitados.size < numCiudades) {
            var mejorCiudad = -1
            var mejorDistancia = Double.MAX_VALUE

            for (ciudad in 0 until numCiudades) {
                if (ciudad !in visitados) {
                    val distancia = calcularDistancia(puntos[ciudadActual], puntos[ciudad])
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

    private fun seleccionTorneo(poblacion: List<IndividuoTSP>, tamanio: Int): IndividuoTSP {
        val participantes = poblacion.shuffled().take(tamanio)
        return participantes.minByOrNull { it.distancia } ?: participantes.first()
    }

    private fun cruzarOX(padre1: IndividuoTSP, padre2: IndividuoTSP): IndividuoTSP {
        val indices = (0 until padre1.orden.size).shuffled().take(2).sorted()
        val a = indices[0]
        val b = indices[1]

        val intermedio = padre1.orden.subList(a, b).toMutableList()
        val resto = padre2.orden.filter { it !in intermedio }.toMutableList()

        val hijo = mutableListOf<Int>()
        hijo.addAll(resto.take(a))
        hijo.addAll(intermedio)
        hijo.addAll(resto.drop(a))

        return IndividuoTSP(hijo)
    }

    private fun mutarIntercambio(individuo: IndividuoTSP, prob: Double) {
        for (i in individuo.orden.indices) {
            if (Random.nextDouble() < prob) {
                val j = Random.nextInt(individuo.orden.size)
                intercambiar(individuo.orden, i, j)
            }
        }
    }

    private fun mutar2Opt(individuo: IndividuoTSP, prob: Double) {
        if (Random.nextDouble() < prob) {
            val i = Random.nextInt(individuo.orden.size - 1)
            val j = Random.nextInt(i + 1, individuo.orden.size)

            // Invertir segmento
            val segmento = individuo.orden.subList(i, j + 1)
            segmento.reverse()
        }
    }

    private fun intercambiar(lista: MutableList<Int>, i: Int, j: Int) {
        val temp = lista[i]
        lista[i] = lista[j]
        lista[j] = temp
    }

    private fun calcularDistancia(p1: PuntoTSP, p2: PuntoTSP): Double {
        val dx = (p1.x - p2.x).toDouble()
        val dy = (p1.y - p2.y).toDouble()
        return sqrt(dx * dx + dy * dy)
    }
}