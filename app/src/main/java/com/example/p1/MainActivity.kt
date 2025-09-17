package com.example.p1

import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import com.example.p1.TSP.TSPSolverModular
import com.example.p1.databinding.ActivityMainBinding
import com.example.p1.db.AppDatabase
import com.example.p1.db.PuntoEntity
import com.example.p1.db.Ruta
import com.example.p1.genetico.AlgoritmoGeneticoConfig
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val puntos = mutableListOf<Punto>()
    private var rutaOptima = listOf<Int>()
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageView.post {
            val width = binding.imageView.width
            val height = binding.imageView.height

            val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            binding.imageView.setImageBitmap(bitmap)

            val paint = Paint().apply {
                color = Color.BLUE
                strokeWidth = 5f
                style = Paint.Style.STROKE
            }

            binding.imageView.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val x = event.x.toInt()
                    val y = event.y.toInt()
                    puntos.add(Punto(x, y))

                    // Redibujar todo el canvas
                    redibujarCanvas(canvas, paint)
                    binding.imageView.invalidate()

                    // Actualizar contador
                    binding.lblmejordistancia.text = "Puntos agregados: ${puntos.size}"

                    v.performClick()
                }
                true
            }

            binding.btnaccion.setOnClickListener {
                if (puntos.size < 2) {
                    Toast.makeText(this, "Debe tocar al menos 2 puntos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Mostrar mensaje de procesamiento
                Toast.makeText(this, "Calculando ruta óptima con algoritmo genético...", Toast.LENGTH_SHORT).show()
                binding.lblmejordistancia.text = "Procesando..."

                // Ejecutar algoritmo genético en hilo secundario
                ejecutarAlgoritmoGeneticoTSP(canvas, paint)
            }

            // Botón limpiar si existe
            binding.root.findViewById<android.widget.Button?>(R.id.btlimpiar)?.setOnClickListener {
                limpiarCanvas(canvas)
            }

            binding.btnGuardarRuta.setOnClickListener {
                if (rutaOptima.isEmpty()) {
                    Toast.makeText(this, "Primero debe calcular una ruta", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                mostrarDialogoGuardarRuta()
            }

            binding.btnVerRutas.setOnClickListener {
                val intent = Intent(this, RutasGuardadasActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun ejecutarAlgoritmoGeneticoTSP(canvas: Canvas, paint: Paint) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Preparar coordenadas para el algoritmo
                val coordenadas = mutableListOf<Int>()
                puntos.forEach { punto ->
                    coordenadas.add(punto.x)
                    coordenadas.add(punto.y)
                }

                // Configurar parámetros adaptativos del algoritmo genético
                val configuracion = AlgoritmoGeneticoConfig.crearConfiguracion(
                    numPuntos = puntos.size,
                    coordenadas = coordenadas
                )

                // Ejecutar algoritmo genético modularizado
                val resultado = TSPSolverModular.resolverTSP(configuracion)

                // Calcular distancia total y métricas
                val distanciaTotal = CalculadorDistancia.calcularDistanciaRuta(resultado, puntos)
                val mejoramiento = calcularMejoramiento(resultado, puntos)

                // Actualizar UI en hilo principal
                withContext(Dispatchers.Main) {
                    rutaOptima = resultado

                    val textoResultado = buildString {
                        append("Ruta óptima: ${rutaOptima.joinToString(" → ")}")
                        append("\nDistancia total: ${String.format("%.2f px", distanciaTotal)}")
                        append("\nMejoramiento: ${String.format("%.1f%%", mejoramiento)}")
                    }

                    binding.lblmejordistancia.text = textoResultado
                    dibujarRutaCompleta(canvas, paint)
                    binding.imageView.invalidate()

                    Toast.makeText(this@MainActivity, "¡Ruta óptima calculada con curvas de Bézier!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error en algoritmo genético: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.lblmejordistancia.text = "Error en el cálculo"
                }
            }
        }
    }

    private fun calcularMejoramiento(rutaOptima: List<Int>, puntos: List<Punto>): Double {
        if (puntos.size < 3) return 0.0

        // Calcular distancia de ruta aleatoria promedio
        var distanciaPromedio = 0.0
        repeat(100) {
            val rutaAleatoria = (0 until puntos.size).shuffled()
            distanciaPromedio += CalculadorDistancia.calcularDistanciaRuta(rutaAleatoria, puntos)
        }
        distanciaPromedio /= 100

        val distanciaOptima = CalculadorDistancia.calcularDistanciaRuta(rutaOptima, puntos)
        return ((distanciaPromedio - distanciaOptima) / distanciaPromedio) * 100
    }

    private fun redibujarCanvas(canvas: Canvas, paint: Paint) {
        canvas.drawColor(Color.WHITE)

        // Dibujar puntos existentes con números
        val paintPunto = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }

        val paintTexto = Paint().apply {
            color = Color.BLACK
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }

        puntos.forEachIndexed { index, punto ->
            // Círculo del punto
            canvas.drawCircle(punto.x.toFloat(), punto.y.toFloat(), 12f, paintPunto)

            // Número del punto
            canvas.drawText(
                index.toString(),
                punto.x.toFloat(),
                punto.y.toFloat() + 10f,
                paintTexto
            )
        }

        // Si hay ruta calculada, dibujarla
        if (rutaOptima.isNotEmpty()) {
            dibujarRutaCompleta(canvas, paint)
        }
    }

    private fun dibujarRutaCompleta(canvas: Canvas, paint: Paint) {
        canvas.drawColor(Color.WHITE)

        // Redibujar puntos
        val paintPunto = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }

        val paintTexto = Paint().apply {
            color = Color.BLACK
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }

        puntos.forEachIndexed { index, punto ->
            canvas.drawCircle(punto.x.toFloat(), punto.y.toFloat(), 12f, paintPunto)
            canvas.drawText(
                index.toString(),
                punto.x.toFloat(),
                punto.y.toFloat() + 10f,
                paintTexto
            )
        }

        if (rutaOptima.isNotEmpty()) {
            val puntosRuta = rutaOptima.map { puntos[it] }

            // 1. Dibujar líneas rectas de la ruta (estructura base)
            val paintLinea = Paint(paint).apply {
                color = Color.BLUE
                strokeWidth = 3f
                alpha = 180
            }

            for (i in 0 until puntosRuta.size - 1) {
                val p1 = puntosRuta[i]
                val p2 = puntosRuta[i + 1]
                canvas.drawLine(
                    p1.x.toFloat(), p1.y.toFloat(),
                    p2.x.toFloat(), p2.y.toFloat(),
                    paintLinea
                )

                // Dibujar flechas direccionales
                dibujarFlechaDireccion(canvas, p1, p2, paintLinea)
            }

            // 2. Dibujar curva de Bézier principal (el objetivo del proyecto)
            dibujarCurvaBezierOptimizada(canvas, puntosRuta)

            // 3. Dibujar segmentos individuales de Bézier entre pares de puntos
            dibujarSegmentosBezier(canvas, puntosRuta)
        }
    }

    private fun dibujarCurvaBezierOptimizada(canvas: Canvas, ruta: List<Punto>) {
        if (ruta.size < 2) return

        val path = Path()
        path.moveTo(ruta[0].x.toFloat(), ruta[0].y.toFloat())

        // Usar más pasos para una curva más suave
        val steps = kotlin.math.max(100, ruta.size * 20)

        for (t in 1..steps) {
            val tNorm = t / steps.toFloat()
            val punto = calcularBezier(ruta, tNorm)
            path.lineTo(punto.x.toFloat(), punto.y.toFloat())
        }

        // Pintura principal para la curva de Bézier
        val paintBezier = Paint().apply {
            color = Color.MAGENTA
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
            pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f) // Línea punteada
        }

        canvas.drawPath(path, paintBezier)
    }

    private fun dibujarSegmentosBezier(canvas: Canvas, ruta: List<Punto>) {
        // Dibujar curvas de Bézier entre cada par de puntos consecutivos
        val paintSegmento = Paint().apply {
            color = Color.CYAN
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
            alpha = 120
        }

        for (i in 0 until ruta.size - 1) {
            val p1 = ruta[i]
            val p2 = ruta[i + 1]

            // Crear curva cuadrática entre dos puntos
            val path = Path()
            path.moveTo(p1.x.toFloat(), p1.y.toFloat())

            // Punto de control en el medio, ligeramente desplazado
            val controlX = (p1.x + p2.x) / 2f + (p2.y - p1.y) * 0.2f
            val controlY = (p1.y + p2.y) / 2f - (p2.x - p1.x) * 0.2f

            path.quadTo(controlX, controlY, p2.x.toFloat(), p2.y.toFloat())
            canvas.drawPath(path, paintSegmento)
        }
    }

    private fun dibujarFlechaDireccion(canvas: Canvas, desde: Punto, hacia: Punto, paint: Paint) {
        val dx = hacia.x - desde.x
        val dy = hacia.y - desde.y
        val longitud = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        if (longitud < 40) return // Solo dibujar flechas en líneas suficientemente largas

        val ux = dx / longitud
        val uy = dy / longitud

        // Posición de la flecha (70% del camino)
        val flechaX = desde.x + dx * 0.7f
        val flechaY = desde.y + dy * 0.7f

        val tamanioFlecha = 12f
        val anguloFlecha = kotlin.math.PI / 5 // 36 grados

        // Calcular puntas de la flecha
        val flecha1X = flechaX - tamanioFlecha * (ux * kotlin.math.cos(anguloFlecha) - uy * kotlin.math.sin(anguloFlecha)).toFloat()
        val flecha1Y = flechaY - tamanioFlecha * (ux * kotlin.math.sin(anguloFlecha) + uy * kotlin.math.cos(anguloFlecha)).toFloat()

        val flecha2X = flechaX - tamanioFlecha * (ux * kotlin.math.cos(-anguloFlecha) - uy * kotlin.math.sin(-anguloFlecha)).toFloat()
        val flecha2Y = flechaY - tamanioFlecha * (ux * kotlin.math.sin(-anguloFlecha) + uy * kotlin.math.cos(-anguloFlecha)).toFloat()

        val paintFlecha = Paint(paint).apply {
            strokeWidth = 3f
            color = Color.BLUE
        }

        canvas.drawLine(flechaX, flechaY, flecha1X, flecha1Y, paintFlecha)
        canvas.drawLine(flechaX, flechaY, flecha2X, flecha2Y, paintFlecha)
    }

    private fun calcularBezier(puntos: List<Punto>, t: Float): Punto {
        val n = puntos.size
        val copia = puntos.map { Punto(it.x, it.y) }.toMutableList()

        for (r in 1 until n) {
            for (i in 0 until n - r) {
                copia[i].x = ((1 - t) * copia[i].x + t * copia[i + 1].x).toInt()
                copia[i].y = ((1 - t) * copia[i].y + t * copia[i + 1].y).toInt()
            }
        }

        return copia[0]
    }

    private fun limpiarCanvas(canvas: Canvas) {
        puntos.clear()
        rutaOptima = emptyList()
        canvas.drawColor(Color.WHITE)
        binding.imageView.invalidate()
        binding.lblmejordistancia.text = "Toca puntos en la pantalla para crear la ruta"
    }

    private fun mostrarDialogoGuardarRuta() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Guardar Ruta")

        val input = EditText(this)
        input.hint = "Nombre de la ruta"
        builder.setView(input)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val nombreRuta = input.text.toString()
            if (nombreRuta.isNotEmpty()) {
                guardarRutaEnDB(nombreRuta)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun guardarRutaEnDB(nombreRuta: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val nuevaRuta = Ruta(nombre = nombreRuta)
                val rutaId = db.rutaDao().insertRuta(nuevaRuta).toInt()

                val puntosEntidad = rutaOptima.map { index ->
                    val punto = puntos[index]
                    PuntoEntity(rutaId = rutaId, x = punto.x, y = punto.y)
                }
                db.rutaDao().insertPuntos(puntosEntidad)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ruta guardada exitosamente", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al guardar la ruta: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}