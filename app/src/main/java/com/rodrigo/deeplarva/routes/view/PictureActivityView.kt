package com.rodrigo.deeplarva.routes.view

import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.databinding.ActivityPicturesBinding
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.routes.CameraActivity
import com.rodrigo.deeplarva.routes.PictureDetailActivity
import com.rodrigo.deeplarva.ui.adapter.PictureAdapterList
import com.rodrigo.deeplarva.ui.listener.ListEventListener


class PictureActivityView(
    private val activity: AppCompatActivity,
    private val binding: ActivityPicturesBinding,
    private val listener: IPictureViewListener
) {
    private var dialog: AddPictureDialog = AddPictureDialog(activity)
    private var list: ListHandlerView<Picture> = ListHandlerView(binding.lvPictures, binding.tvEmptyPicturesList, object: ListEventListener<Picture> {
        override fun onLongClick(item: Picture, position: Int) {
            showOptionsDialog(item)
        }
        override fun onClick(item: Picture, position: Int) {
        }
    })

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
        val adapter = PictureAdapterList(activity, pictures)
        list.populate(pictures, adapter)
    }

    fun getDialog():AddPictureDialog {
        return dialog
    }

    fun onRequestCameraResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        return dialog.onRequestCameraResult(requestCode, permissions, grantResults)
    }

    private fun showOptionsDialog(item: Picture) {
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_picture_options, null)
        val dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnDelete).setOnClickListener {
            listener.onRemovePicture(item)
            dialog.dismiss()
        }

        dialog.show()
    }
}