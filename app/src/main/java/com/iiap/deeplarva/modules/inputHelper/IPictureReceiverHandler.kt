package com.iiap.deeplarva.modules.inputHelper

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap

interface IPictureReceiverHandler {
    val activity: Activity

    fun launch()

    fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): List<Bitmap>

    fun getRequestCode(): Int
}