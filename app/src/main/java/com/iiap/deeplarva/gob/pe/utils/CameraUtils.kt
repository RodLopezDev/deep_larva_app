package com.iiap.deeplarva.utils

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

class CameraUtils {
    companion object {
        fun getMainCameraCharacteristics(activity: Activity): CameraCharacteristics {
            val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            return cameraManager.getCameraCharacteristics(cameraId)
        }
    }
}