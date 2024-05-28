package com.rodrigo.deeplarva.routes.view

import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.databinding.ActivityPicturesBinding

class PictureActivityView(
    private val activity: AppCompatActivity,
    private val binding: ActivityPicturesBinding,
    private val subSampleId: Long
) {

    init {
        activity.setContentView(binding.root)
        activity.setSupportActionBar(binding.toolbar)

        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Sub-Muestra: ${subSampleId}"
        }
    }
}