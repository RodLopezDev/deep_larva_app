package com.odrigo.recognitionappkt.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.odrigo.recognitionappkt.R
import com.odrigo.recognitionappkt.domain.Picture

class PictureAdapterView (private val dataList: List<Picture>) :
    RecyclerView.Adapter<PictureAdapterView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_picture, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
//        holder.textView.text = if (item.hasMetadata) {
//            "Procesado"
//        } else {
//            "No Procesado"
//        }
        holder.textView.text = if(item.hasMetadata) { "Procesado" } else { "Pendiente" }

        var imgPath = if (item.hasMetadata && item.processedFilePath != "") {
            item.processedFilePath
        } else {
            item.filePath
        }
        val bitmap = BitmapFactory.decodeFile(imgPath)
        holder.imgView.setImageBitmap(bitmap)
        if (item.filePath != null) {
            Glide.with(holder.itemView)
                .load(item.filePath)
                .into(holder.imgView)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewTitle)
        val imgView: ImageView = itemView.findViewById(R.id.imgPicture)
    }
}