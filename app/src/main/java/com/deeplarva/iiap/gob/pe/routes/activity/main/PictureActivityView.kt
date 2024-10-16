package com.deeplarva.iiap.gob.pe.routes.activity.main

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.deeplarva.iiap.gob.pe.R
import com.deeplarva.iiap.gob.pe.databinding.ActivityPicturesBinding
import com.deeplarva.iiap.gob.pe.domain.constants.AppConstants
import com.deeplarva.iiap.gob.pe.domain.constants.PermissionsConstans
import com.deeplarva.iiap.gob.pe.domain.view.PictureListEntity
import com.deeplarva.iiap.gob.pe.modules.inputHelper.PictureInputHelper
import com.deeplarva.iiap.gob.pe.routes.activity.picture_detail.PictureDetailActivity
import com.deeplarva.iiap.gob.pe.ui.adapter.PictureAdapterList
import com.deeplarva.iiap.gob.pe.ui.adapter.PictureItemListListener
import com.deeplarva.iiap.gob.pe.ui.widget.listHandler.ListEventListener
import com.deeplarva.iiap.gob.pe.ui.widget.listHandler.ListHandlerView
import com.deeplarva.iiap.gob.pe.utils.UserUtils


class PictureActivityView(
    private val deviceId: String,
    private val activity: PicturesActivity,
    private val binding: ActivityPicturesBinding,
    private val listener: IPictureViewListener
) {
    private val handler = PictureInputHelper(activity)
    private val itemListener = object:
        ListEventListener<PictureListEntity> {
        override fun onLongClick(item: PictureListEntity, position: Int) {
        }
        override fun onClick(item: PictureListEntity, position: Int) {
            val intent = Intent(activity, PictureDetailActivity::class.java)
            intent.putExtra(AppConstants.INTENT_PICTURE_DETAIL, item.picture.id)
            activity.startActivity(intent)
        }
    }
    private var list: ListHandlerView<PictureListEntity> = ListHandlerView(binding.lvPictures, binding.tvEmptyPicturesList, itemListener)

    init {
        activity.setContentView(binding.root)
        activity.setSupportActionBar(binding.toolbar)

        activity.supportActionBar?.apply {
            title = activity.getString(R.string.title_main)
        }

        binding.btnLoadPic.setOnClickListener {
            if(PermissionsConstans.REQUIRE_CONTRACT_FOR_GALLERY){
                activity.photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                return@setOnClickListener
            }
            handler.launchStorage()
        }
        binding.btnTakePicture.setOnClickListener { handler.launchCamera() }
    }

    fun loadPictures(pictures: List<PictureListEntity>, listener: PictureItemListListener) {
        val adapter = PictureAdapterList(activity, pictures, listener, itemListener)
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
            .setPositiveButton("Cerrar") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Copiar") { dialog, _ ->
                UserUtils.copyTextToClipboard(activity, deviceId)
                activity.runOnUiThread {
                    Toast.makeText(activity, "Copiado", Toast.LENGTH_SHORT).show()
                }
            }
            .create()

        dialog.show()
    }
}