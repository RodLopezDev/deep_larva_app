package com.rodrigo.deeplarva.routes.camera.interfaces

import android.media.Image

interface CameraActionListener {
    fun getFileName(): String
    fun onReceivePicture(image: Image)
    fun onFailReceivePicture()
}