package com.rodrigo.deeplarva.utils.image

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity

class ImageHandler(private val activity: AppCompatActivity) {

    private val camera: PhotoByCameraHandler
    private val storage: PhotoByStorageHandler

    private var handlers = mutableListOf<IPhotoHandler>()

    init {
        camera = PhotoByCameraHandler(activity)
        storage = PhotoByStorageHandler(activity)

        handlers.add(camera)
        handlers.add(storage)
    }

    fun launchCamera(){
        camera.launch()
    }

    fun launchStorage(){
        storage.launch()
    }

    fun resolve(requestCode: Int, resultCode: Int, data: Intent?): Bitmap {
        var selected = handlers.find { it.getRequestCode() === requestCode }
            ?: throw Exception("HANDLER_NOT_FOUND")
        return selected.getBitmap(requestCode, resultCode, data)
    }
}