package com.odrigo.recognitionappkt.drivers.photo

import android.content.Intent
import android.graphics.Bitmap

class PhotoFactory {
    private var handlers = mutableListOf<IPhotoHandler>()

    fun add(handler: IPhotoHandler){
        handlers.add(handler)
    }

    fun resolve(requestCode: Int, resultCode: Int, data: Intent?): Bitmap {
        var selected = handlers.find { it.getRequestCode() === requestCode }
            ?: throw Exception("HANDLER_NOT_FOUND")
        return selected.getBitmap(requestCode, resultCode, data)
    }
}