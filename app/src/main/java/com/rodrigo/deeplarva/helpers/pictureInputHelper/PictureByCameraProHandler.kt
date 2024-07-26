package com.rodrigo.deeplarva.helpers.pictureInputHelper

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.routes.activity.CameraActivity
import com.rodrigo.deeplarva.utils.BitmapUtils
import java.io.IOException

class PictureByCameraProHandler(override val activity: Activity): IPictureReceiverHandler {
    companion object {
        val REQUESTCODE = 1000
        val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun launch() {
        val intent = Intent(activity, CameraActivity::class.java)
        activity.startActivityForResult(intent, REQUESTCODE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): List<Bitmap> {
        val result = data?.getStringExtra(Constants.INTENT_CAMERA_PRO_RESULT)
        val files = result?.split(",,,") ?: listOf()

        val bitmaps = files.mapNotNull {
            try {
                val bitmap = BitmapUtils.getBitmapFromPath(it)

                // Corregir la orientación del bitmap
                val rotatedBitmap = bitmap?.let { it1 -> BitmapUtils.correctBitmapOrientation(it1, it) }

                // Auto-recortado de bitmap segun region de interes detectado
                rotatedBitmap?.let { it1 -> BitmapUtils.autoCropImage(activity, it1) }
            } catch (ex: Exception) {
                null
            }
        }

        return bitmaps
    }

    override fun getRequestCode(): Int {
        return REQUESTCODE
    }

    fun onRequestComplete(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    launch()
                } else {
                    Toast.makeText(activity, "Camera permission denied.", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }

    }
}