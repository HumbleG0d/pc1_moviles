package com.example.p1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.p1.adapters.RutaAdapter
import com.example.p1.data.DatabaseHelper
import com.example.p1.databinding.ActivityRutasGuardadasBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RutasGuardadasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRutasGuardadasBinding
    private val db by lazy { DatabaseHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRutasGuardadasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadRutas()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewRutas.layoutManager = LinearLayoutManager(this)
    }

    private fun loadRutas() {
        CoroutineScope(Dispatchers.IO).launch {
            val rutas = db.getRutasConPuntos()
            withContext(Dispatchers.Main) {
                binding.recyclerViewRutas.adapter = RutaAdapter(rutas) { ruta ->
                    val intent = Intent(this@RutasGuardadasActivity, DetalleRutaActivity::class.java)
                    intent.putExtra("ruta_id", ruta.ruta.id)
                    startActivity(intent)
                }
            }
        }
    }
}
