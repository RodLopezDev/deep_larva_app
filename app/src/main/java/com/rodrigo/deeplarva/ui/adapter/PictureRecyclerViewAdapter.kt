package com.rodrigo.deeplarva.ui.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.domain.Constants
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.utils.Colors

class PictureRecyclerViewAdapter (private val dataList: List<Picture>) :
    RecyclerView.Adapter<PictureRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_picture, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.textView.text = Constants.getPictureResults(item.hasMetadata, item.count)
        holder.shadow.setBackgroundColor(if(item.hasMetadata) {Constants.OPACITY_GREEN} else {Constants.OPACITY_RED})

        var imgPath = if (item.hasMetadata && item.processedFilePath != "") {
            item.processedFilePath
        } else {
            item.filePath
        }
        val bitmap = BitmapFactory.decodeFile(imgPath)
        holder.imgView.setImageBitmap(bitmap)
        if (item.filePath != null) {
            Glide.with(holder.itemView)
                .load(item.thumbnailPath)
                .into(holder.imgView)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewTitle)
        val imgView: ImageView = itemView.findViewById(R.id.imgPicture)
        val shadow: RelativeLayout = itemView.findViewById(R.id.rlShadow)
    }
}