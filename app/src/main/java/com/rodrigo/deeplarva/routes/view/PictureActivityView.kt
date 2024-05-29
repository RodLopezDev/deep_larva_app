package com.rodrigo.deeplarva.routes.view

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.databinding.ActivityPicturesBinding
import com.rodrigo.deeplarva.domain.Constants
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.domain.entity.SubSample
import com.rodrigo.deeplarva.ui.adapter.PictureRecyclerViewAdapter
import com.rodrigo.deeplarva.ui.decorator.SpaceItemDecoration


class PictureActivityView(
    private val activity: AppCompatActivity,
    private val binding: ActivityPicturesBinding,
    private val subSampleId: Long
) {
    private var gridLayoutManager: GridLayoutManager
    private var dialog: AddPictureDialog


    private var tvResults: TextView

    init {
        activity.setContentView(binding.root)
        activity.setSupportActionBar(binding.toolbar)

        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Sub-Muestra: ${subSampleId}"
        }

        gridLayoutManager = GridLayoutManager(activity, 2)

        dialog = AddPictureDialog(activity)

        tvResults = binding.tvResultsContent
    }

    fun addViewListener(listener: PictureViewListener){
        binding.fabNewPicture.setOnClickListener { listener.onAddPicture() }
        binding.fabPredict.setOnClickListener { listener.onPredict() }
    }

    fun loadPictures(pictures: List<Picture>) {
        val adapter = PictureRecyclerViewAdapter(pictures)

        val spaceInPixels: Int = activity.resources.getDimensionPixelSize(R.dimen.recycler_view_item_spacing)

        binding.rvPictures.adapter = adapter
        binding.rvPictures.addItemDecoration(SpaceItemDecoration(spaceInPixels))
        binding.rvPictures.layoutManager = gridLayoutManager
    }

    fun getDialog():AddPictureDialog {
        return dialog
    }


    fun refreshResults(subSample: SubSample?) {
        if(subSample == null) return
        tvResults.text = Constants.getResultsView(subSample)
    }
}