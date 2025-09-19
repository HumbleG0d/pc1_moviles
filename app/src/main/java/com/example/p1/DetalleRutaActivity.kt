package com.example.p1

import android.graphics.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import com.example.p1.data.DatabaseHelper
import com.example.p1.databinding.ActivityDetalleRutaBinding
import com.example.p1.model.RutaConPuntos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalleRutaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleRutaBinding
    private val db by lazy { DatabaseHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleRutaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rutaId = intent.getLongExtra("ruta_id", -1L)
        if (rutaId != -1L) {
            loadAndDrawRuta(rutaId)
        }
    }

    private fun loadAndDrawRuta(rutaId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val rutaConPuntos = db.getRutaConPuntosById(rutaId)
            withContext(Dispatchers.Main) {
                rutaConPuntos?.let {
                    drawRuta(it)
                }
            }
        }
    }

    private fun drawRuta(rutaConPuntos: RutaConPuntos) {
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

            dibujarRutaCompleta(canvas, paint, rutaConPuntos.puntos)
            binding.imageViewDetalle.invalidate()
        }
    }

    private fun dibujarRutaCompleta(canvas: Canvas, paint: Paint, puntosModel: List<com.example.p1.model.Punto>) {
        canvas.drawColor(Color.WHITE)

        val puntosParaDibujo = puntosModel.map { Punto(it.x, it.y) }

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

        puntosParaDibujo.forEachIndexed { index, punto ->
            canvas.drawCircle(punto.x.toFloat(), punto.y.toFloat(), 12f, paintPunto)
            canvas.drawText(
                index.toString(),
                punto.x.toFloat(),
                punto.y.toFloat() + 10f,
                paintTexto
            )
        }

        if (puntosParaDibujo.isNotEmpty()) {
            val paintLinea = Paint(paint).apply {
                color = Color.BLUE
                strokeWidth = 3f
                alpha = 180
            }

            for (i in 0 until puntosParaDibujo.size - 1) {
                val p1 = puntosParaDibujo[i]
                val p2 = puntosParaDibujo[i + 1]
                canvas.drawLine(
                    p1.x.toFloat(), p1.y.toFloat(),
                    p2.x.toFloat(), p2.y.toFloat(),
                    paintLinea
                )
                dibujarFlechaDireccion(canvas, p1, p2, paintLinea)
            }

            dibujarCurvaBezierOptimizada(canvas, puntosParaDibujo)
            dibujarSegmentosBezier(canvas, puntosModel)
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

    private fun dibujarSegmentosBezier(canvas: Canvas, ruta: List<com.example.p1.model.Punto>) {
        val paintSegmento = Paint().apply {
            color = Color.CYAN
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
            alpha = 120
        }

        for (i in 0 until ruta.size - 1) {
            val p1Model = ruta[i]
            val p2Model = ruta[i + 1]

            val path = Path()
            path.moveTo(p1Model.x.toFloat(), p1Model.y.toFloat())

            val controlX = p1Model.controlX
            val controlY = p1Model.controlY

            if (controlX != null && controlY != null) {
                path.quadTo(controlX, controlY, p2Model.x.toFloat(), p2Model.y.toFloat())
                canvas.drawPath(path, paintSegmento)
            }
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
