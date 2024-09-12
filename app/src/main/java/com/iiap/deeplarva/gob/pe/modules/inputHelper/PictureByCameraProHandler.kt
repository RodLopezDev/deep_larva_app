package com.iiap.deeplarva.modules.inputHelper

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ExifInterface
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.iiap.deeplarva.domain.constants.AppConstants
import com.iiap.deeplarva.routes.activity.camera.CameraProActivity
import com.iiap.deeplarva.utils.BitmapUtils
import java.io.File
import java.io.IOException

class PictureByCameraProHandler(override val activity: Activity): IPictureReceiverHandler {
    companion object {
        val REQUESTCODE = 1000
        val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun launch() {
        val intent = Intent(activity, CameraProActivity::class.java)
        activity.startActivityForResult(intent, REQUESTCODE)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): List<Bitmap> {
        val result = data?.getStringExtra(AppConstants.INTENT_CAMERA_PRO_RESULT)
        val files = result?.split(",,,") ?: listOf()

        val bitmaps = files.mapNotNull {
            try {
                val bitmap = BitmapUtils.getBitmapFromPath(it)

                // get metadata
                //val metadata = getMetadataFromBitmap(File(it))

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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getMetadataFromBitmap(imageFile: File): Map<String, String> {
        val metadata = mutableMapOf<String, String>()

        try {
            val exif = ExifInterface(imageFile)

            // Retrieve specific metadata
            val date = exif.getAttribute(ExifInterface.TAG_DATETIME)
            val orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION)
            val iso = exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)
            val fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER)
            val exposure = exif.getAttribute(ExifInterface.TAG_EXPOSURE_INDEX)
            val exposureM = exif.getAttribute(ExifInterface.TAG_EXPOSURE_MODE)
            val exposureT = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
            val exposureP = exif.getAttribute(ExifInterface.TAG_EXPOSURE_PROGRAM)
            val exposureB = exif.getAttribute(ExifInterface.TAG_EXPOSURE_BIAS_VALUE)
            val shutterSpeed = exif.getAttribute(ExifInterface.TAG_SHUTTER_SPEED_VALUE)

            // Add them to the map
            metadata["Date"] = date ?: "Unknown"
            metadata["Orientation"] = orientation ?: "Unknown"
            metadata["ISO"] = iso ?: "Unknown"
            metadata["exposure"] = exposure ?: "Unknown"
            metadata["exposureM"] = exposureM ?: "Unknown"
            metadata["exposureT"] = exposureT ?: "Unknown"
            metadata["exposureB"] = exposureB ?: "Unknown"
            metadata["exposureP"] = exposureP ?: "Unknown"
            metadata["F-Number"] = fNumber ?: "Unknown"
            metadata["Shutter Speed"] = shutterSpeed ?: "Unknown"

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return metadata
    }
}