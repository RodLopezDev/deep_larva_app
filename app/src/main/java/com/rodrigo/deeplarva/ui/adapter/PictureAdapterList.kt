package com.rodrigo.deeplarva.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.domain.Constants
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.utils.Time

class PictureAdapterList (
    context: Context,
    private val dataList: List<Picture>,
    private val listener: PictureItemListListener
) :
    ArrayAdapter<Picture>(context, R.layout.item_list_picture, dataList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemView = inflater.inflate(R.layout.item_list_picture, parent, false)
        }

        val currentItem = dataList[position]


        val btnProcess = itemView!!.findViewById<Button>(R.id.btnProcess)
        val llProcessedView = itemView!!.findViewById<LinearLayout>(R.id.llProcessedView)
        val llUnprocessedView = itemView!!.findViewById<LinearLayout>(R.id.llUnprocessedView)

        val tvDatetime = itemView!!.findViewById<TextView>(R.id.tvDatetime)
        val tvDuration = itemView!!.findViewById<TextView>(R.id.tvDuration)
        val tvCount = itemView!!.findViewById<TextView>(R.id.tvCount)

        tvDatetime.text = Time.longFormatTimestamp(currentItem.timestamp)
        if (currentItem.hasMetadata) {
            tvDuration.text = "Tiempo: ${Time.formatDuration(currentItem.time)}"
            tvCount.text = currentItem.count.toString()

            llProcessedView.visibility = View.VISIBLE
            llUnprocessedView.visibility = View.GONE
            btnProcess.visibility = View.GONE
        } else {
            tvDuration.text = "Tiempo: 00:00:00"
            tvCount.text = "-"

            llProcessedView.visibility = View.GONE
            llUnprocessedView.visibility = View.VISIBLE
            btnProcess.visibility = View.VISIBLE
            btnProcess.setOnClickListener {
                listener.onPredict(currentItem)
            }
        }

        if(currentItem.syncWithCloud) {
            itemView.setBackgroundColor(Constants.GREEN_SYNC)
        }

        return itemView!!
    }
}