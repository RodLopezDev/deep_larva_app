package com.rodrigo.deeplarva.routes.view

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.databinding.ActivityPicturesBinding
import com.rodrigo.deeplarva.domain.view.PictureListEntity
import com.rodrigo.deeplarva.ui.adapter.PictureAdapterList
import com.rodrigo.deeplarva.ui.adapter.PictureItemListListener
import com.rodrigo.deeplarva.ui.listener.ListEventListener
import com.rodrigo.deeplarva.utils.image.ImageHandler


class PictureActivityView(
    private val deviceId: String,
    private val activity: AppCompatActivity,
    private val binding: ActivityPicturesBinding,
    private val listener: IPictureViewListener
) {
    private val handler = ImageHandler(activity)
    private var list: ListHandlerView<PictureListEntity> = ListHandlerView(binding.lvPictures, binding.tvEmptyPicturesList, object: ListEventListener<PictureListEntity> {
        override fun onLongClick(item: PictureListEntity, position: Int) {
            showOptionsDialog(item)
        }
        override fun onClick(item: PictureListEntity, position: Int) {
            activity.runOnUiThread {
                Toast.makeText(activity, "Click", Toast.LENGTH_SHORT).show()
            }
            // TODO: DISABLE TEMPORALLY, REQUIRE ESTIMATION TIME TO IMPLEMENT
//            val intent = Intent(activity, PictureDetailActivity::class.java)
//            intent.putExtra(Constants.INTENT_PICTURE_DETAIL, item.id)
//            activity.startActivity(intent)
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