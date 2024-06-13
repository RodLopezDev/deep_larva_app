package com.rodrigo.deeplarva.ui.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.domain.view.PictureListEntity
import com.rodrigo.deeplarva.utils.Time

class PictureAdapterList (
    context: Context,
    private val dataList: List<PictureListEntity>,
    private val listener: PictureItemListListener
) :
    ArrayAdapter<PictureListEntity>(context, R.layout.item_list_picture, dataList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemView = inflater.inflate(R.layout.item_list_picture, parent, false)
        }

        val currentItem = dataList[position]!!
        val picture = currentItem!!.picture

        val llProcessedView = itemView!!.findViewById<LinearLayout>(R.id.llProcessedView)
        val tvDatetime = itemView!!.findViewById<TextView>(R.id.tvDatetime)
        val tvDuration = itemView!!.findViewById<TextView>(R.id.tvDuration)

        if(picture.syncWithCloud) {
            itemView.setBackgroundColor(Constants.GREEN_SYNC)
        }

        tvDatetime.text = Time.longFormatTimestamp(picture.timestamp)
        tvDuration.text = if (picture.hasMetadata)
            "Tiempo: ${Time.formatDuration(picture.time)}"
        else "Tiempo: 00:00:00"

        if(llProcessedView.childCount == 0) {
            if (picture.hasMetadata) {
                llProcessedView.addView(getCountView(context, picture.count))
            } else if(currentItem.state == null){
                llProcessedView.addView(getButtonForPredict(context) { listener.onPredict(picture) })
            } else if (currentItem.state.isProcessing) {
                llProcessedView.addView(getProcessing(context))
            } else {
                llProcessedView.addView(getButtonForPredict(context))
            }
        }

        return itemView!!
    }

    companion object {
        fun getProcessing(context: Context) : LinearLayout {
            val ll = LinearLayout(context)
            ll.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1F)
            ll.orientation = LinearLayout.VERTICAL
            ll.gravity = Gravity.RIGHT
            ll.height

            val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleLarge).apply {
                layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                    gravity = Gravity.RIGHT
                }
                isIndeterminate = true
            }

            ll.addView(progressBar)
            return ll
        }
        fun getButtonForPredict(context: Context, listener: View.OnClickListener? = null): LinearLayout {
            val ll = LinearLayout(context)
            ll.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1F)
            ll.orientation = LinearLayout.VERTICAL
            ll.gravity = Gravity.RIGHT

            val btn = FloatingActionButton(context)
            val btnLL = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            btnLL.gravity = Gravity.RIGHT
            btn.layoutParams = btnLL
            btn.size = FloatingActionButton.SIZE_MINI
            btn.setImageResource(android.R.drawable.ic_media_play)

            if(listener != null) {
                btn.setOnClickListener(listener)
            } else {
                btn.isEnabled = false
                btn.backgroundTintList = context.getColorStateList(R.color.gray500)
            }

            ll.addView(btn)
            return ll
        }

        fun getCountView(context: Context, count: Int): LinearLayout {
            val ll = LinearLayout(context)
            ll.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1F)
            ll.orientation = LinearLayout.VERTICAL

            val textView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textSize = 12f
                setTextColor(resources.getColor(android.R.color.black, null))
                gravity = Gravity.RIGHT
                text = "Total"
            }

            val textView2 = TextView(context).apply {
                id = View.generateViewId()  // Generate a unique ID
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textSize = 32f // textSize in sp, not dp
//                setLineHeight(40) // lineHeight in pixels
//                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
//                setTextColor(resources.getColor(R.color.purple_200, null))
                gravity = Gravity.RIGHT
                text = count.toString()
            }
            ll.addView(textView)
            ll.addView(textView2)
            return ll
        }
    }
}