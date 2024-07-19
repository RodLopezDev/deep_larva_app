package com.rodrigo.deeplarva.modules.camera

import android.media.Image

interface CameraProHardwareListener {
    fun onReceivePicture(image: Image)
    fun onError(message: String, critical:Boolean = false)
    fun onCameraLoaded()
}