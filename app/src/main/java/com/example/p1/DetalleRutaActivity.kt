package com.example.p1

import android.graphics.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import com.example.p1.databinding.ActivityDetalleRutaBinding
import com.example.p1.db.AppDatabase
import com.example.p1.db.PuntoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalleRutaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleRutaBinding
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleRutaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rutaId = intent.getIntExtra("ruta_id", -1)
        if (rutaId != -1) {
            loadAndDrawRuta(rutaId)
        }
    }

    private fun loadAndDrawRuta(rutaId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val rutaConPuntos = db.rutaDao().getRutaConPuntosById(rutaId)
            withContext(Dispatchers.Main) {
                rutaConPuntos?.let {
                    val puntos = it.puntos.map { puntoEntity -> Punto(puntoEntity.x, puntoEntity.y) }
                    drawRuta(puntos)
                }
            }
        }
    }

    private fun drawRuta(puntos: List<Punto>) {
        binding.imageViewDetalle.post {
            val width = binding.imageViewDetalle.width
            val height = binding.imageViewDetalle.height

            val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            binding.imageViewDetalle.setImageBitmap(bitmap)

            val paint = Paint().apply {
                color = Color.BLUE
                strokeWidth = 5f
                style = Paint.Style.STROKE
            }

            dibujarRutaCompleta(canvas, paint, puntos)
            binding.imageViewDetalle.invalidate()
        }
    }

    private fun dibujarRutaCompleta(canvas: Canvas, paint: Paint, puntos: List<Punto>) {
        canvas.drawColor(Color.WHITE)

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

        if (puntos.isNotEmpty()) {
            val paintLinea = Paint(paint).apply {
                color = Color.BLUE
                strokeWidth = 3f
                alpha = 180
            }

            for (i in 0 until puntos.size - 1) {
                val p1 = puntos[i]
                val p2 = puntos[i + 1]
                canvas.drawLine(
                    p1.x.toFloat(), p1.y.toFloat(),
                    p2.x.toFloat(), p2.y.toFloat(),
                    paintLinea
                )
                dibujarFlechaDireccion(canvas, p1, p2, paintLinea)
            }

            dibujarCurvaBezierOptimizada(canvas, puntos)
            dibujarSegmentosBezier(canvas, puntos)
        }
    }

    private fun dibujarCurvaBezierOptimizada(canvas: Canvas, ruta: List<Punto>) {
        if (ruta.size < 2) return

        val path = Path()
        path.moveTo(ruta[0].x.toFloat(), ruta[0].y.toFloat())

        val steps = kotlin.math.max(100, ruta.size * 20)

        for (t in 1..steps) {
            val tNorm = t / steps.toFloat()
            val punto = calcularBezier(ruta, tNorm)
            path.lineTo(punto.x.toFloat(), punto.y.toFloat())
        }

        val paintBezier = Paint().apply {
            color = Color.MAGENTA
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
            pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        }

        canvas.drawPath(path, paintBezier)
    }

    private fun dibujarSegmentosBezier(canvas: Canvas, ruta: List<Punto>) {
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

            val path = Path()
            path.moveTo(p1.x.toFloat(), p1.y.toFloat())

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

        if (longitud < 40) return

        val ux = dx / longitud
        val uy = dy / longitud

        val flechaX = desde.x + dx * 0.7f
        val flechaY = desde.y + dy * 0.7f

        val tamanioFlecha = 12f
        val anguloFlecha = kotlin.math.PI / 5

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
}
