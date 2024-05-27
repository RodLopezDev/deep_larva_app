package com.odrigo.recognitionappkt.routes.pictures.components

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.odrigo.recognitionappkt.R

class ProgressViewComponent(activity: AppCompatActivity) {

    private val layout: RelativeLayout
    private val tvCounter: TextView

    init {
        layout = activity.findViewById(R.id.rl_progress_view)
        tvCounter = activity.findViewById(R.id.tv_counter)
        hide()
    }

    fun show() {
        layout.visibility = View.VISIBLE
        tvCounter.text = "0%"
    }

    fun hide() {
        layout.visibility = View.INVISIBLE
        tvCounter.text = "0%"
    }

    fun updateProgress(value: Int) {
        layout.visibility = View.INVISIBLE
        tvCounter.text = "$value%"
    }
}