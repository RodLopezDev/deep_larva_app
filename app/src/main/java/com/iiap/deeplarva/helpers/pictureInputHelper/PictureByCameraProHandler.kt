package com.iiap.deeplarva.helpers.pictureInputHelper

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.iiap.deeplarva.domain.constants.AppConstants
import com.iiap.deeplarva.domain.constants.SharedPreferencesConstants
import com.iiap.deeplarva.routes.activity.cameraV1.CameraActivity
import com.iiap.deeplarva.routes.activity.cameraV2.CameraProV2Activity
import com.iiap.deeplarva.utils.BitmapUtils
import com.iiap.deeplarva.utils.PreferencesHelper

class PictureByCameraProHandler(override val activity: Activity): IPictureReceiverHandler {
    companion object {
        val REQUESTCODE = 1000
        val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun launch() {
        val helper = PreferencesHelper(activity)
        val isV2 = helper.getBoolean(SharedPreferencesConstants.CONFIG_CAMERA_ACTIVITY_V2)

        if(isV2) {
            val intent = Intent(activity, CameraProV2Activity::class.java)
            activity.startActivityForResult(intent, REQUESTCODE)
            return
        }

        val intent = Intent(activity, CameraActivity::class.java)
        activity.startActivityForResult(intent, REQUESTCODE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): List<Bitmap> {
        val result = data?.getStringExtra(AppConstants.INTENT_CAMERA_PRO_RESULT)
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