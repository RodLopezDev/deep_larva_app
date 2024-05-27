package com.odrigo.recognitionappkt.routes.pictures.components

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.odrigo.recognitionappkt.R
import com.odrigo.recognitionappkt.domain.SubSample

class ResultsViewComponent(activity: AppCompatActivity) {

    private var tvResults: TextView

    init {
        tvResults = activity.findViewById(R.id.tv_results_content)
    }

    fun setResult(subSample: SubSample) {
        tvResults.text = "Moda : ${subSample.mean} individuos\nMax : ${subSample.min} individuos\nMin : ${subSample.max} individuos"
    }
}