package com.rodrigo.deeplarva.helpers.pictureInputHelper

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import com.rodrigo.deeplarva.application.utils.Constants
import com.rodrigo.deeplarva.routes.activity.CameraActivity
import com.rodrigo.deeplarva.utils.BitmapUtils

class PictureByCameraProHandler(override val activity: Activity): IPictureReceiverHandler {
    companion object {
        val REQUESTCODE = 1000
        val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun launch() {
        val intent = Intent(activity, CameraActivity::class.java)
        activity.startActivityForResult(intent, REQUESTCODE)
    }

    override fun getBitmap(requestCode: Int, resultCode: Int, data: Intent?): List<Bitmap> {
        val result = data!!.getStringExtra(Constants.INTENT_CAMERA_PRO_RESULT)
        val files = result!!.split(",,,")

        val bitmaps = files.map {
            try  {
                BitmapUtils.getBitmapFromPath(it)
            } catch (ex: Exception) {
                null
            }
        }

        return bitmaps.filterNotNull()
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