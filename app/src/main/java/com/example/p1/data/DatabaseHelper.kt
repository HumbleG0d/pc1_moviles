package com.example.p1.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "rutas.db"
        private const val DATABASE_VERSION = 1

        // Rutas Table
        const val TABLE_RUTAS = "rutas"
        const val COLUMN_RUTA_ID = "id"
        const val COLUMN_RUTA_NOMBRE = "nombre"

        // Puntos Table
        const val TABLE_PUNTOS = "puntos"
        const val COLUMN_PUNTO_ID = "id"
        const val COLUMN_PUNTO_RUTA_ID = "ruta_id"
        const val COLUMN_PUNTO_X = "x"
        const val COLUMN_PUNTO_Y = "y"
        const val COLUMN_PUNTO_CONTROL_X = "controlX"
        const val COLUMN_PUNTO_CONTROL_Y = "controlY"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createRutasTable = """
            CREATE TABLE $TABLE_RUTAS (
                $COLUMN_RUTA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_RUTA_NOMBRE TEXT NOT NULL
            )
        """.trimIndent()

        val createPuntosTable = """
            CREATE TABLE $TABLE_PUNTOS (
                $COLUMN_PUNTO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PUNTO_RUTA_ID INTEGER,
                $COLUMN_PUNTO_X INTEGER,
                $COLUMN_PUNTO_Y INTEGER,
                $COLUMN_PUNTO_CONTROL_X REAL,
                $COLUMN_PUNTO_CONTROL_Y REAL,
                FOREIGN KEY($COLUMN_PUNTO_RUTA_ID) REFERENCES $TABLE_RUTAS($COLUMN_RUTA_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db?.execSQL(createRutasTable)
        db?.execSQL(createPuntosTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PUNTOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RUTAS")
        onCreate(db)
    }

    // Data access methods will be added here

    fun addRuta(nombreRuta: String): Long {
        val db = this.writableDatabase
        val values = android.content.ContentValues().apply {
            put(COLUMN_RUTA_NOMBRE, nombreRuta)
        }
        return db.insert(TABLE_RUTAS, null, values)
    }

    fun addPuntos(puntos: List<com.example.p1.model.Punto>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            puntos.forEach { punto ->
                val values = android.content.ContentValues().apply {
                    put(COLUMN_PUNTO_RUTA_ID, punto.rutaId)
                    put(COLUMN_PUNTO_X, punto.x)
                    put(COLUMN_PUNTO_Y, punto.y)
                    put(COLUMN_PUNTO_CONTROL_X, punto.controlX)
                    put(COLUMN_PUNTO_CONTROL_Y, punto.controlY)
                }
                db.insert(TABLE_PUNTOS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getRutasConPuntos(): List<com.example.p1.model.RutaConPuntos> {
        val listaRutas = mutableListOf<com.example.p1.model.RutaConPuntos>()
        val db = this.readableDatabase
        db.rawQuery("SELECT * FROM $TABLE_RUTAS", null)?.use { c ->
            while (c.moveToNext()) {
                val rutaId = c.getLong(c.getColumnIndexOrThrow(COLUMN_RUTA_ID))
                val nombreRuta = c.getString(c.getColumnIndexOrThrow(COLUMN_RUTA_NOMBRE))
                val ruta = com.example.p1.model.Ruta(rutaId, nombreRuta)

                val puntos = getPuntosParaRuta(db, rutaId)
                listaRutas.add(com.example.p1.model.RutaConPuntos(ruta, puntos))
            }
        }
        return listaRutas
    }

    fun getRutaConPuntosById(rutaId: Long): com.example.p1.model.RutaConPuntos? {
        val db = this.readableDatabase
        var rutaConPuntos: com.example.p1.model.RutaConPuntos? = null

        db.query(TABLE_RUTAS, arrayOf(COLUMN_RUTA_ID, COLUMN_RUTA_NOMBRE), "$COLUMN_RUTA_ID = ?", arrayOf(rutaId.toString()), null, null, null)?.use { c ->
             if (c.moveToFirst()) {
                val nombreRuta = c.getString(c.getColumnIndexOrThrow(COLUMN_RUTA_NOMBRE))
                val ruta = com.example.p1.model.Ruta(rutaId, nombreRuta)

                val puntos = getPuntosParaRuta(db, rutaId)
                rutaConPuntos = com.example.p1.model.RutaConPuntos(ruta, puntos)
            }
        }
        return rutaConPuntos
    }

    private fun getPuntosParaRuta(db: SQLiteDatabase, rutaId: Long): List<com.example.p1.model.Punto> {
        val listaPuntos = mutableListOf<com.example.p1.model.Punto>()
        db.query(TABLE_PUNTOS, null, "$COLUMN_PUNTO_RUTA_ID = ?", arrayOf(rutaId.toString()), null, null, null)?.use { c ->
            while (c.moveToNext()) {
                val controlXIndex = c.getColumnIndexOrThrow(COLUMN_PUNTO_CONTROL_X)
                val controlYIndex = c.getColumnIndexOrThrow(COLUMN_PUNTO_CONTROL_Y)

                val punto = com.example.p1.model.Punto(
                    id = c.getLong(c.getColumnIndexOrThrow(COLUMN_PUNTO_ID)),
                    rutaId = c.getLong(c.getColumnIndexOrThrow(COLUMN_PUNTO_RUTA_ID)),
                    x = c.getInt(c.getColumnIndexOrThrow(COLUMN_PUNTO_X)),
                    y = c.getInt(c.getColumnIndexOrThrow(COLUMN_PUNTO_Y)),
                    controlX = if (c.isNull(controlXIndex)) null else c.getFloat(controlXIndex),
                    controlY = if (c.isNull(controlYIndex)) null else c.getFloat(controlYIndex)
                )
                listaPuntos.add(punto)
            }
        }
        return listaPuntos
    }
}
