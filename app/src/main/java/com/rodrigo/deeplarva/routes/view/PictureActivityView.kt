package com.rodrigo.deeplarva.routes.view

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.rodrigo.deeplarva.databinding.ActivityPicturesBinding
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.ui.adapter.PictureRecyclerViewAdapter

class PictureActivityView(
    private val activity: AppCompatActivity,
    private val binding: ActivityPicturesBinding,
    private val subSampleId: Long
) {
    private var gridLayoutManager: GridLayoutManager
    private var dialog: AddPictureDialog

    init {
        activity.setContentView(binding.root)
        activity.setSupportActionBar(binding.toolbar)

        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Sub-Muestra: ${subSampleId}"
        }

        gridLayoutManager = GridLayoutManager(activity, 2)

        dialog = AddPictureDialog(activity)
    }

    fun addViewListener(listener: PictureViewListener){
        binding.fabNewPicture.setOnClickListener { listener.onAddPicture() }
        binding.fabPredict.setOnClickListener { listener.onPredict() }
    }

    fun loadPictures(pictures: List<Picture>) {
        val adapter = PictureRecyclerViewAdapter(pictures)
        binding.rvPictures.adapter = adapter
        binding.rvPictures.layoutManager = gridLayoutManager
    }

    fun getDialog():AddPictureDialog {
        return dialog
    }


}