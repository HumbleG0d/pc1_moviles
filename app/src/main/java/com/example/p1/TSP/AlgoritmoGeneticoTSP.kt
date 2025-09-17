package com.example.p1.TSP

import com.example.p1.TSP.ConfiguracionTSP
import com.example.p1.genetico.GeneradorPoblacion
import com.example.p1.genetico.Individuo
import com.example.p1.genetico.OperadoresCruce
import com.example.p1.genetico.OperadoresMutacion
import com.example.p1.Punto
import com.example.p1.RequestData
import com.example.p1.ResponseData
import com.example.p1.genetico.SelectorPadres
import kotlin.random.Random

class AlgoritmoGeneticoTSP {

    fun resolverTSP(configuracion: ConfiguracionTSP): ResponseData {
        // Generar población inicial
        var poblacion = GeneradorPoblacion.generarPoblacionGreedy(
            configuracion.numCiudades,
            configuracion.tamPoblacion,
            configuracion.coordenadas
        ).toMutableList()

        // Calcular distancia inicial para toda la población
        poblacion.forEach { it.calcularDistancia(configuracion.coordenadas) }

        // Evolucionar por el número de generaciones especificado
        repeat(configuracion.generaciones) {
            poblacion = evolucionarGeneracion(poblacion, configuracion)
        }

        // Encontrar el mejor individuo
        val mejor = poblacion.minByOrNull { it.distancia } ?: poblacion.first()

        return ResponseData(prediction = mejor.orden)
    }

    private fun evolucionarGeneracion(
        poblacion: MutableList<Individuo>,
        configuracion: ConfiguracionTSP
    ): MutableList<Individuo> {
        // Ordenar por distancia (menor es mejor)
        poblacion.sortBy { it.distancia }

        // Mantener élites
        val elites = SelectorPadres.seleccionElitista(poblacion, 0.2)
        val nuevaGen = elites.map { it.clonar() }.toMutableList()

        // Generar nueva descendencia
        while (nuevaGen.size < configuracion.tamPoblacion) {
            val padre1 = SelectorPadres.seleccionTorneo(poblacion)
            val padre2 = SelectorPadres.seleccionTorneo(poblacion)

            val hijo = OperadoresCruce.cruceOX(padre1, padre2)
            OperadoresMutacion.mutacionIntercambio(hijo, configuracion.probMutacion)

            // Aplicar mutación adicional ocasionalmente
            if (Random.nextDouble() < 0.1) {
                OperadoresMutacion.mutacion2Opt(hijo, 0.5)
            }

            hijo.calcularDistancia(configuracion.coordenadas)
            nuevaGen.add(hijo)
        }

        return nuevaGen
    }

    fun predict(requestData: RequestData): ResponseData {
        val configuracion = convertirRequestData(requestData)
        return resolverTSP(configuracion)
    }

    private fun convertirRequestData(requestData: RequestData): ConfiguracionTSP {
        val numCiudades = requestData.data[0].toInt()
        val tamPoblacion = requestData.data[1].toInt()
        val probMutacion = requestData.data[2].toFloat()
        val generaciones = requestData.data[3].toInt()

        val coordenadas = mutableListOf<Punto>()
        for (i in requestData.data2.indices step 2) {
            if (i + 1 < requestData.data2.size) {
                coordenadas.add(Punto(requestData.data2[i], requestData.data2[i + 1]))
            }
        }

        return ConfiguracionTSP(
            numCiudades = numCiudades,
            tamPoblacion = tamPoblacion,
            probMutacion = probMutacion,
            generaciones = generaciones,
            coordenadas = coordenadas
        )
    }
}