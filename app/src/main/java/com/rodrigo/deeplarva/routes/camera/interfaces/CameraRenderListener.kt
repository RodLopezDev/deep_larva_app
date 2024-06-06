package com.rodrigo.deeplarva.routes.camera.interfaces

import android.hardware.camera2.CameraDevice
import com.rodrigo.deeplarva.routes.camera.Camera

interface CameraRenderListener {
    fun onOpened(camera: Camera, cameraDevice: CameraDevice)
    fun onDisconnected(camera: CameraDevice)
    fun onError(camera: CameraDevice, error: Int)
}