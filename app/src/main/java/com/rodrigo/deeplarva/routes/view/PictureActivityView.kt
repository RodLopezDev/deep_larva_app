package com.rodrigo.deeplarva.routes.view

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.databinding.ActivityPicturesBinding
import com.rodrigo.deeplarva.domain.MessageFactory
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.ui.adapter.PictureRecyclerViewAdapter
import com.rodrigo.deeplarva.ui.decorator.SpaceItemDecoration


class PictureActivityView(
    private val activity: AppCompatActivity,
    private val binding: ActivityPicturesBinding
) {
    private var gridLayoutManager: GridLayoutManager
    private var dialog: AddPictureDialog

    init {
        activity.setContentView(binding.root)
        activity.setSupportActionBar(binding.toolbar)

        activity.supportActionBar?.apply {
            title = "Muestras"
        }

        gridLayoutManager = GridLayoutManager(activity, 2)

        dialog = AddPictureDialog(activity)

        val spaceInPixels: Int = activity.resources.getDimensionPixelSize(R.dimen.recycler_view_item_spacing)
        binding.rvPictures.addItemDecoration(SpaceItemDecoration(spaceInPixels))
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

    fun onRequestCameraResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        return dialog.onRequestCameraResult(requestCode, permissions, grantResults)
    }
}