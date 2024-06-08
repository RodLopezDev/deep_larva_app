package com.rodrigo.deeplarva.utils.image

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap

interface IPhotoHandler {
    val activity: Activity

    fun launch()

    fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): List<Bitmap>

    fun getRequestCode(): Int
}