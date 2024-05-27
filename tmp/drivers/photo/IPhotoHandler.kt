package com.odrigo.recognitionappkt.drivers.photo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore

interface IPhotoHandler {
    val activity: Activity

    fun launch()

    fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): Bitmap

    fun getRequestCode(): Int
}