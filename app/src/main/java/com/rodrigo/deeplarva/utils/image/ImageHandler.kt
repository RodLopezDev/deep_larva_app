package com.rodrigo.deeplarva.utils.image

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity

class ImageHandler(private val activity: AppCompatActivity) {

    private val storage: PhotoByStorageHandler = PhotoByStorageHandler(activity)
    private val cameraPro: PhotoByCameraProHandler = PhotoByCameraProHandler(activity)

    private var handlers = mutableListOf<IPhotoHandler>()

    init {
        handlers.add(cameraPro)
        handlers.add(storage)
    }

    fun launchCamera(){
        cameraPro.launch()
    }

    fun launchStorage(){
        storage.launch()
    }

    fun resolve(requestCode: Int, resultCode: Int, data: Intent?): List<Bitmap> {
        var selected = handlers.find { it.getRequestCode() === requestCode }
            ?: throw Exception("HANDLER_NOT_FOUND")
        return selected.getBitmap(requestCode, resultCode, data)
    }

    fun onRequestComplete(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        return cameraPro.onRequestComplete(requestCode, permissions, grantResults)
    }
}