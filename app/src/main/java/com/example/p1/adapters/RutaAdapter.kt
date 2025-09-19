package com.example.p1.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.p1.R
import com.example.p1.model.RutaConPuntos

class RutaAdapter(
    private val rutas: List<RutaConPuntos>,
    private val onItemClick: (RutaConPuntos) -> Unit
) : RecyclerView.Adapter<RutaAdapter.RutaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ruta, parent, false)
        return RutaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RutaViewHolder, position: Int) {
        val ruta = rutas[position]
        holder.bind(ruta)
        holder.itemView.setOnClickListener { onItemClick(ruta) }
    }

    override fun getItemCount(): Int = rutas.size

    class RutaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreRuta: TextView = itemView.findViewById(R.id.nombreRuta)

        fun bind(ruta: RutaConPuntos) {
            nombreRuta.text = ruta.ruta.nombre
        }
    }
}
