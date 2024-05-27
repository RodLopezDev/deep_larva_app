package com.odrigo.recognitionappkt.routes.pictures

import androidx.appcompat.app.AppCompatActivity
import com.odrigo.recognitionappkt.domain.SubSample

class State(activity: AppCompatActivity) {

    private var my: AppCompatActivity
    var subSampleId: Long = 0
        private set

    lateinit var subSample: SubSample

    init {
        my = activity
        subSampleId = activity.intent.getLongExtra("subSampleId", 0)
    }
}