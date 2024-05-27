package com.rodrigo.deeplarva.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.domain.view.SubSampleItemList

class SubSampleAdapterList (context: Context, private val dataList: List<SubSampleItemList>) :
    ArrayAdapter<SubSampleItemList>(context, R.layout.item_list_subsample, dataList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemView = inflater.inflate(R.layout.item_list_subsample, parent, false)
        }

        val currentItem = dataList[position]

        val textViewTitle = itemView!!.findViewById<TextView>(R.id.textViewTitle)
        val textViewDescription = itemView.findViewById<TextView>(R.id.textViewDescription)
        val textViewLabel = itemView.findViewById<TextView>(R.id.tvLabelSubMuestra)

        textViewTitle.text = "Submuestra ${position + 1}"
        textViewDescription.text = "Contenito: ${currentItem.counts} Imágenes\nMuestras: ${currentItem.mean} detectadas"
        textViewLabel.text = if (currentItem.mean > 0) {
            "Procesado"
        } else {
            "Pendiente"
        }

        itemView.setOnClickListener {
//            val intent = Intent(context, Presenter::class.java)
//            intent.putExtra("subSampleId", currentItem.id)
//            context.startActivity(intent)
        }

        return itemView
    }
}