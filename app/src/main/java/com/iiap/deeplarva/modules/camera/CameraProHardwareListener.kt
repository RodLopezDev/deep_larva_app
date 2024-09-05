package com.iiap.deeplarva.modules.camera

import android.media.Image

interface CameraProHardwareListener {
    fun onReceivePicture(image: Image, sensorOrientation: Int, windowRotation: Int)
    fun onError(message: String, critical:Boolean = false)
    fun onCameraLoaded()
}