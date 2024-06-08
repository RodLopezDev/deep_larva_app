package com.rodrigo.deeplarva.routes.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.databinding.ActivityPicturesBinding
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.routes.CameraActivity
import com.rodrigo.deeplarva.routes.PictureDetailActivity
import com.rodrigo.deeplarva.ui.adapter.PictureAdapterList
import com.rodrigo.deeplarva.ui.listener.ListEventListener


class PictureActivityView(
    private val activity: AppCompatActivity,
    private val binding: ActivityPicturesBinding
) {
    private var dialog: AddPictureDialog = AddPictureDialog(activity)
    private var list: ListHandlerView<Picture> = ListHandlerView(binding.lvPictures, binding.tvEmptyPicturesList)

    init {
        activity.setContentView(binding.root)
        activity.setSupportActionBar(binding.toolbar)

        activity.supportActionBar?.apply {
            title = "Muestras"
        }
    }

    fun addViewListener(listener: PictureViewListener){
        binding.fabNewPicture.setOnClickListener { listener.onAddPicture() }
        binding.fabPredict.setOnClickListener { listener.onPredict() }
    }

    fun loadPictures(pictures: List<Picture>) {
        val adapter = PictureAdapterList(activity, pictures, object: ListEventListener<Picture> {
            override fun onLongClick(item: Picture, position: Int) {
            }
            override fun onClick(item: Picture, position: Int) {
                val intent = Intent(activity, PictureDetailActivity::class.java)
                intent.putExtra("pictureId", item.id)
                activity.startActivity(intent)
            }
        })
        list.populate(pictures, adapter)
    }

    fun getDialog():AddPictureDialog {
        return dialog
    }

    fun onRequestCameraResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        return dialog.onRequestCameraResult(requestCode, permissions, grantResults)
    }
}