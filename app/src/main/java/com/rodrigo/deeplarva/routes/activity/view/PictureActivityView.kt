package com.rodrigo.deeplarva.routes.activity.view

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.databinding.ActivityPicturesBinding
import com.rodrigo.deeplarva.domain.view.PictureListEntity
import com.rodrigo.deeplarva.helpers.pictureInputHelper.PictureInputHelper
import com.rodrigo.deeplarva.ui.adapter.PictureAdapterList
import com.rodrigo.deeplarva.ui.adapter.PictureItemListListener
import com.rodrigo.deeplarva.ui.widget.listHandler.ListEventListener
import com.rodrigo.deeplarva.ui.widget.listHandler.ListHandlerView


class PictureActivityView(
    private val deviceId: String,
    private val activity: AppCompatActivity,
    private val binding: ActivityPicturesBinding,
    private val listener: IPictureViewListener
) {
    private val handler = PictureInputHelper(activity)
    private var list: ListHandlerView<PictureListEntity> = ListHandlerView(binding.lvPictures, binding.tvEmptyPicturesList, object:
        ListEventListener<PictureListEntity> {
        override fun onLongClick(item: PictureListEntity, position: Int) {
//            showOptionsDialog(item) TO-DO: Disabled temporally
        }
        override fun onClick(item: PictureListEntity, position: Int) {
        }
    })

    init {
        activity.setContentView(binding.root)
        activity.setSupportActionBar(binding.toolbar)

        activity.supportActionBar?.apply {
            title = "Muestras"
        }

        binding.btnLoadPic.setOnClickListener { handler.launchStorage() }
        binding.btnTakePicture.setOnClickListener { handler.launchCamera() }
    }

    fun loadPictures(pictures: List<PictureListEntity>, listener: PictureItemListListener) {
        val adapter = PictureAdapterList(activity, pictures, listener)
        list.populate(pictures, adapter)
    }

    fun onRequestCameraResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        return handler.onRequestComplete(requestCode, permissions, grantResults)
    }

    fun resolve(requestCode: Int, resultCode: Int, data: Intent?): List<Bitmap> {
        return handler.resolve(requestCode, resultCode, data)
    }

    private fun showOptionsDialog(item: PictureListEntity) {
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_picture_options, null)
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Opciones")
            .setView(dialogView)
            .create()

        val btnRemove = dialogView.findViewById<Button>(R.id.btnDelete)
        btnRemove.setOnClickListener {
            listener.onRemovePicture(item.picture)
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showInfoDialog() {
        val dialog = AlertDialog.Builder(activity)
            .setTitle("DeviceID")
            .setMessage(deviceId)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }
}