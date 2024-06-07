package com.rodrigo.deeplarva.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.ui.listener.ListEventListener
import com.rodrigo.deeplarva.utils.Time
import java.text.SimpleDateFormat
import java.util.Locale

class PictureAdapterList (context: Context, private val dataList: List<Picture>, private val listener: ListEventListener<Picture>) :
    ArrayAdapter<Picture>(context, R.layout.item_list_picture, dataList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemView = inflater.inflate(R.layout.item_list_picture, parent, false)
        }

        val currentItem = dataList[position]

        val tvDatetime = itemView!!.findViewById<TextView>(R.id.tvDatetime)
        val tvDuration = itemView!!.findViewById<TextView>(R.id.tvDuration)
        val tvCount = itemView!!.findViewById<TextView>(R.id.tvCount)

        tvDatetime.text = Time.longFormatTimestamp(currentItem.timestamp)
        if (currentItem.hasMetadata) {
            tvDuration.text = "Tiempo: ${Time.formatDuration(currentItem.time)}"
            tvCount.text = currentItem.count.toString()
        } else {
            tvDuration.text = "Tiempo: 00:00:00"
            tvCount.text = "-"
        }

        return itemView!!
    }
}