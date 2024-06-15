package com.rodrigo.deeplarva.modules.camera.interfaces

import android.hardware.camera2.CameraCharacteristics
import android.media.Image

interface CameraProListener {
    fun onDetectCamera(cameraCharacteristics: CameraCharacteristics)
    fun onReceivePicture(image: Image)
    fun onLogError(message: String)
    fun onError()
    fun onCameraLoaded()
}