package com.rodrigo.deeplarva.routes.cameraV2

import android.hardware.camera2.CameraCharacteristics
import android.media.Image

interface CameraProListener {
    fun onDetectCamera(cameraCharacteristics: CameraCharacteristics)
    fun onReceivePicture(image: Image)
    fun onLogError(message: String)
    fun onError()
}