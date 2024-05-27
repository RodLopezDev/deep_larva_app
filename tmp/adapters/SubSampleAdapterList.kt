package com.odrigo.recognitionappkt.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.odrigo.recognitionappkt.R
import com.odrigo.recognitionappkt.domain.views.SubSampleItemList
import com.odrigo.recognitionappkt.view.ActivityChangesListener
import com.odrigo.recognitionappkt.view.DeletableElement

class SubSampleAdapterList (context: Context, private val dataList: List<DeletableElement<SubSampleItemList>>, listenerProp: ActivityChangesListener<SubSampleItemList>)
    : ArrayAdapter<DeletableElement<SubSampleItemList>>(context, R.layout.item_list_subsample, dataList) {

    private var modeDelete = false
    private var listener: ActivityChangesListener<SubSampleItemList>

    init {
        listener = listenerProp
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemView = inflater.inflate(R.layout.item_list_subsample, parent, false)
        }

        var currentItem = super.getItem(position)
        if(currentItem != null) {
            val subsample = currentItem.item
            val textViewTitle = itemView!!.findViewById<TextView>(R.id.textViewTitle)
            val textViewDescription = itemView.findViewById<TextView>(R.id.textViewDescription)
            val textViewLabel = itemView.findViewById<TextView>(R.id.tvLabelSubMuestra)

            textViewTitle.text = "Submuestra ${position + 1}"
            textViewDescription.text = "Contenito: ${subsample.counts} Imágenes\nMuestras: ${subsample.mean} detectadas"
            textViewLabel.text = if (subsample.mean > 0) {
                "Procesado"
            } else {
                "Pendiente"
            }

            if (currentItem.hasFlag) {
                textViewLabel.setTextColor(Color.parseColor("#FF5722"))
            }else{
                textViewLabel.setTextColor(Color.parseColor("#000000"))
            }

            itemView.setOnClickListener {
                listener.setOnClickListener(subsample, position)
            }

            itemView.setOnLongClickListener {
                listener.setOnLongClickListener(position)
                true
            }
        }

        return itemView!!
    }
}